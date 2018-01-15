package com.ming.common.solution.controller;

import com.ming.common.solution.TestCoreConfig;
import com.ming.common.solution.config.SecurityConfig;
import com.ming.common.solution.entity.UserRole;
import com.ming.common.solution.service.LoginService;
import me.jiangcai.lib.test.SpringWebTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author CJ
 */
@ContextConfiguration(classes = {TestCoreConfig.class, SecurityConfig.class})
@WebAppConfiguration
public class LoginControllerTest extends SpringWebTest {

    @Autowired
    private LoginService loginService;

    @Test
    public void loginStatus() throws Exception {
        mockMvc.perform(
                get("/loginStatus")
        )
                .andExpect(status().is(240));

        String username = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();

        loginService.newUser(username, password, UserRole.developer);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(
                post("/login")
                        .param("username", username)
                        .param("password", password + password)
        )
                .andExpect(status().is(240))
                .andReturn().getRequest().getSession();

        mockMvc.perform(
                post("/login")
                        .param("username", username)
                        .param("password", password)
                        .session(session)
        )
                .andExpect(status().is(200))
                .andDo(print())
        ;

        mockMvc.perform(
                get("/loginStatus")
                        .session(session)
        )
                .andExpect(status().is(200))
                .andDo(print());


    }
}