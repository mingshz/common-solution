package com.ming.common.solution.service;

import com.ming.common.solution.entity.User;
import com.ming.common.solution.entity.UserRole;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author CJ
 */
public interface LoginService extends UserDetailsService {

    /**
     * 新增一个用户
     *
     * @param name        名字
     * @param rawPassword 明文密码
     * @param role        角色
     * @return 已保存的用户
     */
    @Transactional
    User newUser(String name, String rawPassword, UserRole role);

}
