package com.ming.common.solution.entity;

import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author CJ
 */
@Setter
@Getter
@Entity
public class User implements UserDetails, CrudFriendly<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 40)
    private String username;
    private String password;
    private boolean enabled;
    private UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null)
            return Collections.emptySet();
        return role.roles().stream()
                .map(role -> {
                    role = role.toUpperCase(Locale.CHINA);
                    if (role.startsWith("ROLE_"))
                        return role;
                    return "ROLE_" + role;
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
