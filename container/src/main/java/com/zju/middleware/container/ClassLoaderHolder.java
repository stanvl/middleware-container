// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ClassLoaderHolder.java

package com.zju.middleware.container;

public class ClassLoaderHolder {

    public ClassLoaderHolder() {
    }

    public static void setBizLoader(ClassLoader clazzLoader) {
        bizLoader = clazzLoader;
    }

    public static ClassLoader getBizLoader() {
        return bizLoader;
    }

    /**
     * MiddlewareWebappClassLoader
     */
    private static ClassLoader bizLoader;

    public static void cleanup() {
        bizLoader = null;
    }

}
