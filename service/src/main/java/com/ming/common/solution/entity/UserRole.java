package com.ming.common.solution.entity;

import org.luffy.libs.libseext.CollectionUtils;

import java.util.Set;

/**
 * @author CJ
 */
public enum UserRole {
    root,
    manager,
    deveplor;
    /**
     * 就是可登录
     */
    public static final String ROLE_USER = "USER";
    /**
     * 就是可管理
     */
    public static final String ROLE_MANAGE = "MANAGE";

    public Set<String> roles() {
        if (this == root)
            return CollectionUtils.mutliSet(ROLE_USER, ROLE_MANAGE, "ROOT");
        if (this == manager)
            return CollectionUtils.mutliSet(ROLE_USER, ROLE_MANAGE, "ROOT");
        return CollectionUtils.mutliSet(ROLE_USER);
    }
}
