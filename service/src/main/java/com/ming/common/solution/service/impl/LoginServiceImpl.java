package com.ming.common.solution.service.impl;

import com.ming.common.solution.entity.User;
import com.ming.common.solution.entity.UserRole;
import com.ming.common.solution.repository.UserRepository;
import com.ming.common.solution.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author CJ
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(username + " can not find.");
        return user;
    }

    @Override
    public User newUser(String name, String rawPassword, UserRole role) {
        if (name == null || rawPassword == null)
            throw new IllegalArgumentException();
        if (userRepository.findByUsername(name) != null)
            throw new IllegalArgumentException();
        User user = new User();
        user.setEnabled(true);
        user.setUsername(name);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role == null ? UserRole.developer : role);
        return userRepository.save(user);
    }
}
