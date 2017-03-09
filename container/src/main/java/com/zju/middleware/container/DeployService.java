package com.zju.middleware.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author cangxing
 * @date 2017-03-09 12:00
 */
public class DeployService {
    public static final DeployService INSTANCE = new DeployService();

    private static final Logger logger = LoggerFactory.getLogger(DeployService.class);

    private DeployService() {
    }

    public void deployPlugin(File plugin) {
        String moduleName = plugin.getName();
        String pluginPath = plugin.getAbsolutePath();
        File jarsPath = new File(pluginPath + File.separator + "lib");
        File[] jars = jarsPath.listFiles();
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        String[] importPackages = null;
        String content = readFileByLines(pluginPath + File.separator + "conf" + File.separator + "import.properties");
        if (content != null) {
            importPackages = content.split(",");
        }
        content = readFileByLines(pluginPath + File.separator + "conf" + File.separator + "export.properties");
        File exportJar = null;
        if (content != null) {
            exportJar = new File(pluginPath + File.separator + "lib" + File.separator + content);
        }
        ModuleClassLoader classLoader = ClassLoaderService.INSTANCE.createModuleClassLoader(moduleName, urls, importPackages, exportJar);
        try {
            JarFile jarFile = new JarFile(exportJar);
            Enumeration entries = jarFile.entries();
            do {
                if (!entries.hasMoreElements())
                    break;
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    System.out.println(entry.getName());
                    String className = convertClassName(entry);
                    try {
                        Class clazz = classLoader.loadClass(className);
                        SharedClassService.INSTANCE.putIfAbsent(moduleName, clazz);
                        if (logger.isDebugEnabled())
                            logger.debug("ClassExporter", moduleName, new Object[]{
                                    "export class: {}", className
                            });
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertClassName(JarEntry entry) {
        if (entry.isDirectory())
            return null;
        String entryName = entry.getName();
        if (!entryName.endsWith(".class"))
            return null;
        if (entryName.charAt(0) == '/')
            entryName = entryName.substring(1);
        entryName = entryName.replace("/", ".");
        return entryName.substring(0, entryName.length() - 6);
    }

    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                return tempString;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }
}
