package com.gzf.video.core.controller.action;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.http.HttpExchange;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class ActionGenerator {

    private static final String actionClassPath = ConfigManager.getControllerConf()
            .getString("package").replace('.', '/');

    private static final String objectName = Type.getInternalName(Object.class);
    private static final Type   objectType = Type.getType(Object.class);
    private static final String functionName = Type.getInternalName(Function.class);
    private static final String applyMethodDesc = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class));

    private static final String actionMethodName = "doAction";
    private static final Type   actionType = Type.getType(Action.class);
    private static final Type   actionMethodType;

    private static int actionIdx = 0;

    static  {
        try {
            Method actionMethod = Action.class.getMethod(actionMethodName, HttpExchange.class);
            actionMethodType = Type.getType(actionMethod);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    public static Action newAction(
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

    private static Handle metafactoryHandle = findMetaFactoryHandle();

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

    private static final Method defineClassMethod;

    static {
        try {
            defineClassMethod = Proxy.class.getDeclaredMethod(
                    "defineClass0",
                    ClassLoader.class, String.class, byte[].class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
        defineClassMethod.setAccessible(true);
    }

    @SuppressWarnings("unchecked")
    private static Class<Function<Object, Action>> defineAction(
            final Class controllerClass,
            final String name,
            final byte[] code) throws InvocationTargetException, IllegalAccessException {
        ClassLoader loader = controllerClass.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        return (Class<Function<Object, Action>>)
                defineClassMethod.invoke(null, loader, name, code, 0, code.length);
    }
}
