package com.ming.common.solution;

import me.jiangcai.lib.test.config.H2DataSourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.sql.DataSource;

/**
 * @author CJ
 */
@Configuration
@ImportResource("classpath:/datasource_local.xml")
public class TestCoreConfig extends H2DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        return memDataSource("cs");
    }
}
