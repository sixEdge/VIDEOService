package com.gzf.video.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.gzf.video.util.StringUtil.SEP;

public abstract class Scanner {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private static final String CLASSPATH = ProjectDependent.CLASSPATH;

    private static final String SOURCE_EX_NAME = ".class";


    public void scan(final String pkg) throws Exception {
        String packagePath = pkg.replace('.', SEP);
        String absolutePath = CLASSPATH + packagePath;

        File dir = new File(absolutePath);

        if (!dir.exists())
            throw new Exception("package does not exist: " + dir.getAbsolutePath());
        else if (!dir.isDirectory())
            throw new Exception("base package is not a dir: " + dir.getAbsolutePath());

        scan0(dir, packagePath);
    }

    private void scan0(final File dir, final String packagePath) throws Exception {
        if (dir.isHidden())
            throw new Exception("package is hidden: " + dir.getAbsolutePath());

        File[] fs = dir.listFiles();

        if (fs == null) return;

        for (File f : fs) {
            if (f.isDirectory()) {
                scan0(f, packagePath);
                continue;
            } else if (!f.getName().endsWith(SOURCE_EX_NAME)) {
                continue;
            } else if (f.isHidden() || !f.canRead()) {
                logger.warn("can't read file {}", f.getAbsolutePath());
                continue;
            }

            String sourcePath = getSourcePath(f.getAbsolutePath(), packagePath);
            Class<?> clazz = classLoader.loadClass(sourcePath);

            if (isThis(clazz)) {
                // do something with the target class
                process(clazz);
            }
        }
    }

    private static String getSourcePath(final String absolutePath, final String basePackagePath) {
        return absolutePath
                .substring(absolutePath.indexOf(basePackagePath), absolutePath.lastIndexOf(SOURCE_EX_NAME))
                .replace(SEP, '.');
    }

    /**
     * Is this the class we are searching for? =. =
     * @param clazz the class type we are searching
     * @return true or false
     */
    protected abstract boolean isThis(final Class<?> clazz);

    /**
     * Do something with the class.
     * @param clazz the class type we are searching
     */
    protected abstract void process(final Class<?> clazz);
}
