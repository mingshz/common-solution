package com.ming.common.solution.config;

import com.ming.common.solution.Version;
import me.jiangcai.crud.CrudConfig;
import me.jiangcai.lib.jdbc.JdbcSpringConfig;
import me.jiangcai.lib.resource.web.WebResourceSpringConfig;
import me.jiangcai.lib.spring.logging.LoggingConfig;
import me.jiangcai.lib.sys.SystemStringConfig;
import me.jiangcai.lib.sys.service.SystemStringService;
import me.jiangcai.lib.upgrade.UpgradeSpringConfig;
import me.jiangcai.lib.upgrade.VersionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author CJ
 */
@Configuration
@ComponentScan({
        "com.ming.common.solution.service", "com.ming.common.solution.controller"
})
@EnableJpaRepositories(basePackages = "com.ming.common.solution.repository")
@EnableTransactionManagement(mode = AdviceMode.PROXY)
@EnableAspectJAutoProxy
@Import({UpgradeSpringConfig.class, JdbcSpringConfig.class, LoggingConfig.class
        , WebResourceSpringConfig.class
        , SystemStringConfig.class, CrudConfig.class})
public class CoreConfig {

    @Autowired
    private SystemStringService systemStringService;

    @Bean
    @SuppressWarnings("unchecked")
    public VersionInfoService versionInfoService() {
        final String versionKey = "version.database";
        return new VersionInfoService() {

            @Override
            public <T extends Enum> T currentVersion(Class<T> type) {
                String systemString = systemStringService.getSystemString(versionKey, String.class, null);
                if (systemString == null)
                    return null;
                return (T) Version.valueOf(systemString);
            }

            @Override
            public <T extends Enum> void updateVersion(T currentVersion) {
                systemStringService.updateSystemString(versionKey, currentVersion.name());
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
