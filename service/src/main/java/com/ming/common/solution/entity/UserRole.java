package com.ming.common.solution.entity;

import org.luffy.libs.libseext.CollectionUtils;

import java.util.Set;

/**
 * @author CJ
 */
public enum UserRole {
    root,
    manager,
    developer;
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

    @Override
    public String toString() {
        switch (this) {
            case root:
                return "build-in";
            case manager:
                return "管理员";
            case developer:
                return "开发人员";
            default:
                return "用户";
        }
    }
}
