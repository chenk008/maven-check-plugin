package com.alibaba.maven.plugins.pom.versions.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * 加载.properties资源文件工具类
 * 
 * 
 */
public final class PropertiesLoaderUtil {
    /**
     * 在classpath路径加载资源
     * 
     * @param fileName 资源文件名
     * @return Properties
     * @throws IOException
     */
    public static Properties getProperties(String fileName) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName is null or ''");
        }
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            Properties pro = new Properties();
            pro.load(is);
            return pro;
        } else {
            throw new FileNotFoundException(fileName + " is NotFound");
        }

    }

}
