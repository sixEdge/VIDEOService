package com.gzf.video.core.bean;

import com.gzf.video.core.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public class BeanScanner extends ClassScanner {
    private static final Logger logger = LoggerFactory.getLogger(BeanScanner.class);

    private final BeanFactory beanFactory;

    private BeanScanner(final BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Scan bean.
     * @param beanFactory beanFactory
     */
    public static void scanBeans(final BeanFactory beanFactory, final String... packages) {
        BeanScanner beanScanner = new BeanScanner(beanFactory);
        Stream.of(packages).forEach(pkg -> {
            try {
                beanScanner.scan(pkg);
            } catch (Throwable t) {
                logger.error("scanBeans", t);
                System.exit(1);
            }
        });
    }

    @Override
    protected boolean isThis(final Class<?> clazz) {
        return !clazz.isInterface()
            && !Modifier.isAbstract(clazz.getModifiers())
            && clazz.getDeclaredAnnotation(Bean.class) != null;
    }

    @Override
    protected void process(final Class<?> clazz) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            beanFactory.put(clazz.getName(), constructor.newInstance());
        } catch ( NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
            logger.error("BeanScanner", e);
        }
    }
}
