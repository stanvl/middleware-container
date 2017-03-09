package com.meili.inc.tomcat.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author cangxing
 * @date 2016-11-30 23:12
 */
@Configuration
@ComponentScan(basePackages = {"com.meili.inc.tomcat.web"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = EnableWebMvc.class)})
public class RootConfig {
}
