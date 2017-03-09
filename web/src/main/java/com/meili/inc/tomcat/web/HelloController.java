package com.meili.inc.tomcat.web;

import com.zju.example.middleware1.MiddlewareOneApi;
import com.zju.example.middleware2.MiddlewareTwoApi;
import io.netty.bootstrap.ServerBootstrap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cangxing
 * @date 2016-12-01 20:07
 */
@Controller
@RequestMapping("/hello")
public class HelloController {
    @ResponseBody
    @RequestMapping("/echo")
    public String index(){
        ClassLoader classLoader = getClass().getClassLoader();
        while (classLoader != null){
            System.out.println(classLoader.getClass());
            classLoader = classLoader.getParent();
        }
        System.out.println("app netty classloader:"+ServerBootstrap.class.getClassLoader());
        System.out.println(MiddlewareOneApi.class.getClassLoader());
        new MiddlewareOneApi().echo();
        System.out.println(MiddlewareTwoApi.class.getClassLoader());
        new MiddlewareTwoApi().echo();
        return "hello";
    }
}
