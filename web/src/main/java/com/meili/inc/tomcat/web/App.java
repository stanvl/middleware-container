package com.meili.inc.tomcat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cangxing
 * @date 2016-11-30 13:48
 */

@Controller
@RequestMapping("/app")
public class App {
    @ResponseBody
    @RequestMapping("/demo")
    public String index(){
        return "demo";
    }
}
