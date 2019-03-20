package com.ming.common.solution;

import com.ming.common.solution.entity.ImageRegister;
import com.ming.common.solution.repository.ImageRegisterRepository;
import com.ming.common.solution.service.DeployService;
import me.jiangcai.lib.test.config.H2DataSourceConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @author CJ
 */
@Configuration
@ImportResource("classpath:/datasource_local.xml")
public class TestCoreConfig extends H2DataSourceConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Primary
    public DeployService deployService() {
        return new DeployService() {
            @Override
            public void imageUpdate(@NotNull ImageRegister image, @NotNull String version) {

            }

            @Nullable
            @Override
            public ImageRegister findImage(@NotNull String region, @NotNull String namespace, @NotNull String name) {
                return applicationContext.getBean(ImageRegisterRepository.class).findByRegionAndNamespaceAndName(region, namespace, name);
            }
        };
    }

    @Bean
    public DataSource dataSource() {
        return memDataSource("cs");
    }
}
