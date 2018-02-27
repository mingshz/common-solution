package com.ming.common.solution.service;

import com.ming.common.solution.Version;
import com.ming.common.solution.controller.AliHook;
import com.ming.common.solution.entity.Project;
import com.ming.common.solution.entity.ProjectService;
import com.ming.common.solution.entity.RuntimeEnvironment;
import com.ming.common.solution.entity.User;
import com.ming.common.solution.entity.UserRole;
import com.ming.common.solution.repository.ProjectRepository;
import me.jiangcai.lib.jdbc.JdbcService;
import me.jiangcai.lib.sys.service.SystemStringService;
import me.jiangcai.lib.upgrade.VersionUpgrade;
import me.jiangcai.lib.upgrade.service.UpgradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author CJ
 */
@Service
public class InitService {
    @Autowired
    private UpgradeService upgradeService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private Environment environment;
    @Autowired
    private RuntimeEnvironmentService runtimeEnvironmentService;
    @Autowired
    private SystemStringService systemStringService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private JdbcService jdbcService;

    @PostConstruct
    public void init() {
        String hostsHome = environment.getProperty("hosts.home");
        if (hostsHome != null) {
            runtimeEnvironmentService.addHosts(hostsHome);
        }
        //noinspection Convert2Lambda
        upgradeService.systemUpgrade(new VersionUpgrade<Version>() {
            @Override
            public void upgradeToVersion(Version version) throws Exception {
                switch (version) {
                    case init:
                        break;
                    case addP1:
                        systemStringService.updateSystemString(AliHook.AllowKey, "123");
                        Project project = projectRepository.getOne("shopping-beauty");
                        // 添加image
                        ProjectService client = runtimeEnvironmentService.addService(project, imageService.addImage(
                                "cn-shanghai", "mingshz", "shopping-beauty-client", "auto_deploy@mingshz"
                                , "Abcd_1234"
                        ), "front");
                        ProjectService manager = runtimeEnvironmentService.addService(project, imageService.addImage(
                                "cn-shanghai", "mingshz", "shopping-beauty-manager", "auto_deploy@mingshz"
                                , "Abcd_1234"
                        ), "manager");
                        ProjectService server = runtimeEnvironmentService.addService(project, imageService.addImage(
                                "cn-shanghai", "mingshz", "shopping-beauty-server", "auto_deploy@mingshz"
                                , "Abcd_1234"
                        ), "server");
                        // 添加测试周期
                        RuntimeEnvironment test = runtimeEnvironmentService.addRuntimeEnvironment(project
                                , runtimeEnvironmentService.getHost("118.178.57.117"), "测试", "sb_test");
                        // 添加版本
                        runtimeEnvironmentService.updateServiceVersion(test, client, "latest");
                        runtimeEnvironmentService.updateServiceVersion(test, manager, "latest");
                        runtimeEnvironmentService.updateServiceVersion(test, server, "latest");
                        break;
                    case email:
                        // 关联以及 email
                        jdbcService.tableAlterAddColumn(User.class, "emailAddress", null);
                        jdbcService.runJdbcWork(connection -> {
                            try (Connection connection1 = connection.getConnection()) {
                                try (Statement statement = connection1.createStatement()) {
                                    statement.executeUpdate("ALTER TABLE PROJECTSERVICE ADD COLUMN PROJECT_ID VARCHAR(30)");
                                    statement.executeUpdate("ALTER TABLE RUNTIMEENVIRONMENT ADD COLUMN PROJECT_ID VARCHAR(30)");
                                    statement.executeUpdate("ALTER TABLE PROJECTSERVICE ADD CONSTRAINT FK_PROJECTSERVICE_PROJECT_ID FOREIGN KEY (PROJECT_ID) REFERENCES PROJECT (ID)");
                                    statement.executeUpdate("ALTER TABLE RUNTIMEENVIRONMENT ADD CONSTRAINT FK_RUNTIMEENVIRONMENT_PROJECT_ID FOREIGN KEY (PROJECT_ID) REFERENCES PROJECT (ID)");
                                }
                            }
                        });
                        break;
                    default:
                }
            }
        });


        try {
            loginService.loadUserByUsername("root");
        } catch (UsernameNotFoundException ex) {
            loginService.newUser("root", "root", UserRole.root);
        }

    }

}
