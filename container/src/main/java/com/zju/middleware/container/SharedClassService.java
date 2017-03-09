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
        //key：modelName,value:exportClassList
        moduleCachedModel = new HashMap();
    }

    public Map getSharedClassMap() {
        return Collections.unmodifiableMap(cachedClasses);
    }

    public int getSharedClassCount() {
        return cachedClasses.size();
    }

    public Class getClass(String fullClassName) {
        Class result = null;
        if (fullClassName != null)
            result = (Class) cachedClasses.get(fullClassName);
        return result;
    }

    public Class putIfAbsent(String moduleName, Class clazz) {
        if (moduleName == null || clazz == null)
            return null;
        Class oldClazz = (Class) cachedClasses.putIfAbsent(clazz.getName(), clazz);
        if (oldClazz == null) {
            List moduleClassList = (List) moduleCachedModel.get(moduleName);
            if (moduleClassList == null) {
                moduleClassList = new ArrayList();
                moduleCachedModel.put(moduleName, moduleClassList);
            }
            moduleClassList.add(clazz);
        }
        return oldClazz;
    }

    public List getSharedClassList(String moduleName) {
        List classList = Collections.emptyList();
        if (moduleName != null && moduleCachedModel.get(moduleName) != null)
            classList = Collections.unmodifiableList((List) moduleCachedModel.get(moduleName));
        return classList;
    }

    private ConcurrentHashMap cachedClasses;
    private Map moduleCachedModel;
}
