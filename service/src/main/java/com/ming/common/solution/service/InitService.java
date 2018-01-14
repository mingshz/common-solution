package com.ming.common.solution.service;

import com.ming.common.solution.Version;
import com.ming.common.solution.entity.UserRole;
import me.jiangcai.lib.upgrade.VersionUpgrade;
import me.jiangcai.lib.upgrade.service.UpgradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author CJ
 */
@Service
public class InitService {
    @Autowired
    private UpgradeService upgradeService;
    @Autowired
    private LoginService loginService;

    @PostConstruct
    public void init() {
        //noinspection Convert2Lambda
        upgradeService.systemUpgrade(new VersionUpgrade<Version>() {
            @Override
            public void upgradeToVersion(Version version) throws Exception {

            }
        });

        try {
            loginService.loadUserByUsername("root");
        } catch (UsernameNotFoundException ex) {
            loginService.newUser("root", "root", UserRole.root);
        }

    }

}
