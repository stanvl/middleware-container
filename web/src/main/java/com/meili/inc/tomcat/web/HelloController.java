package com.meili.inc.tomcat.web;

import com.zju.example.middleware1.MiddlewareOneApi;
import com.zju.example.middleware2.MiddlewareTwoApi;
import io.netty.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cangxing
 * @date 2016-12-01 20:07
 */
@Controller
@RequestMapping("/test")
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @ResponseBody
    @RequestMapping()
    public String index() {
        StringBuilder sb = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        while (classLoader != null) {
            logger.info(classLoader.getClass().toString());
            classLoader = classLoader.getParent();
        }
        logger.info("app netty classloader:" + ServerBootstrap.class.getClassLoader());

        logger.info(MiddlewareOneApi.class.getClassLoader().toString());
        sb.append(MiddlewareOneApi.class.getClassLoader().toString());
        sb.append("<br/>");
        sb.append("\n\r");
        sb.append(new MiddlewareOneApi().echo());
        sb.append("<br/>");
        sb.append("\n\r");


        logger.info(MiddlewareTwoApi.class.getClassLoader().toString());
        sb.append("<br/>");
        sb.append("\n\r");
        sb.append(MiddlewareTwoApi.class.getClassLoader().toString());
        sb.append("<br/>");
        sb.append("\n\r");
        sb.append(new MiddlewareTwoApi().echo());

        return sb.toString();
    }
}
