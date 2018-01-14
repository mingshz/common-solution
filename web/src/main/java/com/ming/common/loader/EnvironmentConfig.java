package com.ming.common.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

/**
 * 服务器运行时所依赖的配置
 *
 * @author CJ
 */
@Configuration
@ImportResource("classpath:datasource.xml")
class EnvironmentConfig {


    @Autowired
    public void forEnv(Environment environment) {
        System.out.println("current url:" + environment.getProperty("url"));
    }

}
