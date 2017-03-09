package com.zju.example.middleware1;

import io.netty.util.Version;
import org.springframework.core.SpringVersion;

/**
 * @author cangxing
 * @date 2017-03-09 14:29
 */
public class MiddlewareOneApi {
    public String echo(){
        System.out.println("MiddlewareOneApi:");
        System.out.println(SpringVersion.getVersion());
        System.out.println(Version.class.getClassLoader());
        String version = Version.identify(MiddlewareOneApi.class.getClassLoader()).toString();
        System.out.println(version);
        return version;
    }
}
