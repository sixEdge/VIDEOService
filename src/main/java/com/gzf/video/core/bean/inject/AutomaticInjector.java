package com.gzf.video.core.bean.inject;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.bean.BeanFactory;
import com.gzf.video.core.bean.BeanScanner;
import com.gzf.video.core.controller.Controller;

import java.lang.reflect.Field;
import java.util.Map;

public class AutomaticInjector {

    private static String[] packages;

    static {
        packages = ConfigManager.appConf.getConfig("bean").getStringList("scanPackages")
                .toArray(new String[0]);
    }

    private static final BeanFactory beanFactory = new BeanFactory();

    public static void init() {
        BeanScanner.scanBeans(beanFactory, packages);
        injectEachOther();
    }

    public static void autoInject(final Object obj, final Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()){
            field.setAccessible(true);
            if (field.getAnnotation(Autowire.class) != null) {
                String fieldTypeName = field.getType().getName();
                try {
                    field.set(obj, beanFactory.get(fieldTypeName));
                } catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            }
        }
    }

    private static void injectEachOther() {
        Map<String, Object> mapper = beanFactory.getMapper();
        mapper.forEach((name, obj) -> {
            Class<?> clazz = obj.getClass();

            if (clazz.getDeclaredAnnotation(Controller.class) == null
                    && clazz.getDeclaredAnnotation(Component.class) == null) {
                return;
            }

            autoInject(obj, clazz);
        });
    }

    public static Object getBean(final String className) {
        return beanFactory.get(className);
    }
}
