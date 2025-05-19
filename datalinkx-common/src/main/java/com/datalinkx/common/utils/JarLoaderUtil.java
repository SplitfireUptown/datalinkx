package com.datalinkx.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JarLoaderUtil {
    /**
     * 加载指定目录下的所有 JAR 文件
     * @param directoryPath 包含 JAR 文件的目录路径
     * @throws IOException 如果在加载 JAR 文件时发生 I/O 错误
     */
    public static List<URL> loadJarsFromDirectory(String directoryPath) throws IOException, NoSuchMethodException {
        List<URL> result = new ArrayList<URL>();
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("指定的目录不存在或不是一个有效的目录: " + directoryPath);
        }

        File[] jarFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles != null) {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

            for (File jarFile : jarFiles) {
                try {
                    URL url = jarFile.toURI().toURL();
                    result.add(url);
                } catch (MalformedURLException e) {
                    log.info(e.getMessage() + "无效的 JAR 文件 URL: ", e);
                } catch (Exception e) {
                    log.error(e.getMessage() + "无法加载 JAR 文件: ", e);
                }
            }
        }
        return result;
    }
}