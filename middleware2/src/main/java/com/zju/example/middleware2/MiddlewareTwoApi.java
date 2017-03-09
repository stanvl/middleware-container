package com.zju.example.middleware2;

import io.netty.util.Version;
import org.springframework.core.SpringVersion;

/**
 * @author cangxing
 * @date 2017-03-09 14:29
 */
public class MiddlewareTwoApi {
    public String echo(){
        System.out.println("MiddlewareTwoApi:");
        System.out.println(SpringVersion.getVersion());
        System.out.println(Version.class.getClassLoader());
        String version = Version.identify(MiddlewareTwoApi.class.getClassLoader()).toString();
        System.out.println(version);
        return version;
    }
}
