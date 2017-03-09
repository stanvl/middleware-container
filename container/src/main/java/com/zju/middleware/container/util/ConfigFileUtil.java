package com.zju.middleware.container.util;

import java.io.File;

/**
 * @author cangxing
 * @date 2017-03-09 20:37
 */
public class ConfigFileUtil {
    private static final String CATALINA_HOME = System.getProperty("catalina.base");
    private static final String DEPLOY = "deploy";
    private static final String CONTAIN_SAR = "container.sar";
    private static final String PLUGINS = "PLUGINS";
    private static final String LIB = "lib";
    private static final String CONF = "conf";
    private static final String IMPORT_CONFIG = "import.properties";
    private static final String EXPORT_CONFIG = "export.properties";

    //${CATALINA_HOME}/deploy/container.sar/plugins
    public static String pluginRoot(){
        return CATALINA_HOME + File.separator + DEPLOY + File.separator + CONTAIN_SAR + File.separator + PLUGINS;
    }

    //${CATALINA_HOME}/deploy/container.sar/plugins/${middleware}/lib
    public static String middlewareLib(String middlewareRootPath) {
        return middlewareRootPath + File.separator + LIB;
    }

    //${CATALINA_HOME}/deploy/container.sar/plugins/${middleware}/conf/import.properties
    public static String middlewareImportConfigPath(String middlewareRootPath) {
        return middlewareRootPath + File.separator + CONF + File.separator + IMPORT_CONFIG;
    }

    //${CATALINA_HOME}/deploy/container.sar/plugins/${middleware}/conf/export.properties
    public static String middlewareExportConfigPath(String middlewareRootPath) {
        return middlewareRootPath + File.separator + CONF + File.separator + EXPORT_CONFIG;
    }

    //${CATALINA_HOME}/deploy/container.sar/plugins/${middleware}/lib/${middleware-api.jar}
    public static String middlewareExportJar(String middlewareRootPath, String exportJar) {
        return middlewareRootPath + File.separator + LIB + File.separator + exportJar;
    }
}
