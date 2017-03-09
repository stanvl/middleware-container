package com.zju.middleware;

import java.util.Map;

/**
 * @author cangxing
 * @date 2017-03-08 14:11
 * 用于管理各个中间件提供给业务方的api类
 */
public class ExportClassRepository {
    public Class resolveClass(String className) {
        if (middlewareRepoAccessor != null)
            return (Class) middlewareRepoAccessor.get(className);
        else
            return null;
    }

    //key：exportClassName,value:exportClass
    public void setMiddlewareRepoAccessor(Map middlewareRepoAccessor) {
        this.middlewareRepoAccessor = middlewareRepoAccessor;
    }

    private Map middlewareRepoAccessor;
}
