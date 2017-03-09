package com.zju.middleware.container;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cangxing
 * @date 2017-03-09 11:29
 */
public class SharedClassService {
    public static final SharedClassService INSTANCE = new SharedClassService();
    /**
     * 当tomcat采用AliWebappClassLoader加载一个中间件暴露的api类时，会通过api类名modelClassName在cachedClasses找到api类
     */
    private SharedClassService() {
        //key：exportClassName,value:exportClass
        cachedClasses = new ConcurrentHashMap();
    }

    public Map getSharedClassMap() {
        return Collections.unmodifiableMap(cachedClasses);
    }

    public Class getClass(String fullClassName) {
        Class result = null;
        if (fullClassName != null)
            result = cachedClasses.get(fullClassName);
        return result;
    }

    public Class putIfAbsent(String moduleName, Class clazz) {
        if (moduleName == null || clazz == null)
            return null;
        Class oldClazz = cachedClasses.putIfAbsent(clazz.getName(), clazz);
        return oldClazz;
    }

    private ConcurrentHashMap<String, Class> cachedClasses;
}
