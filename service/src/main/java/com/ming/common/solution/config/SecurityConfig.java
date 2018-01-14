package com.ming.common.solution.config;

import com.ming.common.solution.controller.LoginController;
import com.ming.common.solution.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 安全相关的配置
 *
 * @author CJ
 */
@Configuration
@Import({CoreConfig.class, SecurityConfig.SecurityAdviceConfig.class})
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private LoginService loginService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configAuthenticationManagerBuilder(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(loginService)
                .passwordEncoder(passwordEncoder);
    }

    @EnableWebSecurity
    @Import({CoreConfig.class})
    public static class SecurityAdviceConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private LoginController loginController;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                    .antMatchers("/public/**").permitAll()
                    .anyRequest()
                    .permitAll()
//                .anyRequest()
//                .hasRole(UserRole.ROLE_USER)
                    .and()
                    // Possibly more configuration ...
                    .csrf().disable()
                    .formLogin() // enable form based log in
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/loginStatus", true)
                    .successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                            loginController.loginStatus(authentication.getPrincipal(), response);
                        }
                    })
                    .failureHandler((request, response, exception) -> response.sendError(240))
                    // set permitAll for all URLs associated with Form Login
                    .permitAll()
            ;
        }
    }


}
