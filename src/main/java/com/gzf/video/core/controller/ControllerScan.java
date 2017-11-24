package com.gzf.video.core.controller;

import com.gzf.video.core.annotation.Controller;
import com.gzf.video.core.annotation.action.Get;
import com.gzf.video.core.annotation.action.Post;
import com.gzf.video.core.request.Request;
import com.gzf.video.core.dispatcher.CustomParametersParser;
import com.gzf.video.core.dispatcher.ActionDispatcher;
import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.ConfigManager;
import com.typesafe.config.Config;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.gzf.video.util.StringUtil.CLASS_PATH;
import static com.gzf.video.util.StringUtil.SEP;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

public class ControllerScan {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** <em>NOTE: Memory leak</em> */
    private final List<Object> controllerList = new LinkedList<>();

    private ActionDispatcher actionDispatcher;
    private CustomParametersParser customParametersParser = new CustomParametersParser();

    private final Config controllerConfig = ConfigManager.getControllerConf();
    private final Config actionConfig = controllerConfig.getConfig("action");

    private final String actionClassPath =
            actionConfig.getString("classPath").replace('.', '/');

    private static final String objectName = Type.getInternalName(Object.class);
    private static final Type objectType = Type.getType(Object.class);
    private final Type actionType = Type.getType(Action.class);
    private final String functionName = Type.getInternalName(Function.class);


    private static final String actionMethodName = "doAction";
    private final Type actionMethodType;
    private final String applyMethodDesc = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class));


    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private static final String PACKAGE = "package";

    private static final String SOURCE_EX_NAME = ".class";
    private static int actionIdx = 0;




    public ControllerScan(final ActionDispatcher actionDispatcher) throws NoSuchMethodException {
        this.actionDispatcher = actionDispatcher;
        Method actionMethod = Action.class.getMethod(actionMethodName, Request.class);
        actionMethodType = Type.getType(actionMethod);
    }


    public void refresh() throws Exception {
        scanControllers();
        logger.info("{} controllers has been scanned", controllerList.size());
        System.gc();
    }


    private void scanControllers() throws Exception {
        String controllerClassPackage =
                controllerConfig.getString(PACKAGE);
        if (controllerClassPackage == null)
            throw new Error("no controller package specific");

        controllerClassPackage = controllerClassPackage.replace('.', SEP);

        final String basePath = CLASS_PATH + controllerClassPackage;

        scanControllers(basePath, controllerClassPackage);

    }

    private void scanControllers(final String basePath, final String basePackagePath)
            throws Exception {
        File dir = new File(basePath);

        if (!dir.exists())
            throw new Error("package does not exist: " + dir.getAbsolutePath());
        else if (!dir.isDirectory())
            throw new Error("base package is not a dir: " + dir.getAbsolutePath());

        scanControllers0(new File(basePath), basePackagePath);
    }

    private void scanControllers0(final File dir, final String basePackagePath)
            throws Exception {
        if (dir.isHidden())
            throw new Error("package is hidden " + dir.getAbsolutePath());

        File[] fs = dir.listFiles();

        if (fs == null) return;

        for (File f : fs) {
            if (f.isDirectory()) {
                scanControllers0(f, basePackagePath);
            }

            if (f.isHidden() || !f.canRead()) {
                logger.warn("can't read file {}", f.getAbsolutePath());
                continue;
            } else if (!f.getName().endsWith(SOURCE_EX_NAME)) {
                continue;
            }

            String sourcePath = getSourcePath(f.getAbsolutePath(), basePackagePath);
            Class clazz = classLoader.loadClass(sourcePath);

            if (clazz.getDeclaredAnnotation(Controller.class) == null) {
                continue;
            }
            if (clazz.isInterface())
                throw new Error("controller must be declared as a class " + clazz);

            scanActions(clazz);
        }
    }

    private void scanActions(final Class<?> controllerClass) throws Exception {
        Method[] ms = controllerClass.getDeclaredMethods();

        if (ms.length < 1)
            return;

        Constructor controllerConstructor = controllerClass.getDeclaredConstructor();
        controllerConstructor.setAccessible(true);
        Object controllerObj = controllerConstructor.newInstance();
        controllerList.add(controllerObj);

        for (Method m : ms) {
            m.setAccessible(true);
            Get get;
            Post post;
            String url;
            if ((get = m.getDeclaredAnnotation(Get.class)) != null) {
                url = get.value();
            } else if ((post = m.getDeclaredAnnotation(Post.class)) != null) {
                url = post.value();
            } else {
                continue;
            }

            checkUseCustomParameter(url);

            Action action = newAction(controllerClass, controllerObj, m);
            actionDispatcher.setAction(customParametersParser.parseParams(m, url), action, get != null);
        }


    }

    private Action newAction(
            final Class controllerClass,
            final Object controllerObj,
            final Method m)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {

        final String actionName = actionClassPath + "/Action$" + actionIdx++;

        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL,
                actionName,
                null,
                objectName, new String[] { functionName });

        String desc = Type.getMethodDescriptor(Type.getType(void.class));
        GeneratorAdapter gac = createMethod(cw, ACC_PUBLIC, "<init>", desc);
        gac.loadThis();
        gac.invokeConstructor(objectType, new org.objectweb.asm.commons.Method("<init>", desc));
        gac.returnValue();
        gac.endMethod();

        Handle bsmHandle = new Handle(
                H_INVOKEVIRTUAL,
                Type.getInternalName(controllerClass),
                m.getName(),
                Type.getMethodDescriptor(m),
                false);

        Type controllerType = Type.getType(controllerClass);

        String actionMethodDesc = Type.getMethodDescriptor(actionType, controllerType);

        final Object[] bsmArgs =
                new Object[] { actionMethodType, bsmHandle, actionMethodType };

        GeneratorAdapter gam = createMethod(
                        cw,
                        ACC_PUBLIC,
                        "apply",
                        applyMethodDesc);
        gam.loadArg(0);
        gam.checkCast(controllerType);
        gam.invokeDynamic(actionMethodName, actionMethodDesc, metafactoryHandle, bsmArgs);
        gam.returnValue();
        gam.endMethod();

        cw.visitEnd();

        Class<Function<Object, Action>> lambdaGeneratorClass =
                defineAction(controllerClass, actionName, cw.toByteArray());

        return lambdaGeneratorClass.newInstance().apply(controllerObj);
    }

    private void checkUseCustomParameter(final String url) {
        if (ActionDispatcher.notUseCustomParameter
                && (url.contains("{") || url.contains("}") || url.contains("?") || url.contains("&")))
            throw new Error("don't use custom-parameter and request-parameter on an action");
    }

    private String getSourcePath(final String string, final String basePackagePath) {
        return string
                .substring(string.indexOf(basePackagePath), string.lastIndexOf(SOURCE_EX_NAME))
                .replace(SEP, '.');
    }

    private static GeneratorAdapter createMethod(ClassVisitor cv,
                                                 int access,
                                                 String name,
                                                 String desc) {
        return
            new GeneratorAdapter(
                cv.visitMethod(access, name, desc, null, null),
                access,
                name,
                desc);
    }

    private Handle metafactoryHandle = findMetaFactoryHandle();

    private static Handle findMetaFactoryHandle() {
        Method metafactory = null;
        for (Method m : LambdaMetafactory.class.getDeclaredMethods()) {
            if (m.getName().equals("metafactory")) {
                metafactory = m;
                break;
            }
        }

        if (metafactory == null)
            throw new Error("method \"metafactory()\" not found");

        return new Handle(H_INVOKESTATIC, Type.getInternalName(metafactory.getDeclaringClass()), metafactory.getName(), Type.getMethodDescriptor(metafactory), false);
    }

    private final Method defineClassMethod;

    {
        defineClassMethod = Proxy.class.getDeclaredMethod(
                                "defineClass0",
                                ClassLoader.class, String.class, byte[].class, int.class, int.class);
        defineClassMethod.setAccessible(true);
    }

    @SuppressWarnings("unchecked")
    private Class<Function<Object, Action>> defineAction(
            final Class controllerClass,
            final String name,
            final byte[] code)
            throws InvocationTargetException, IllegalAccessException {
        ClassLoader loader = controllerClass.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        return (Class<Function<Object, Action>>)
                defineClassMethod.invoke(null, loader, name, code, 0, code.length);
    }
}
