package com.ming.common.solution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.common.solution.TestCoreConfig;
import com.ming.common.solution.config.CoreConfig;
import com.ming.common.solution.entity.User;
import com.ming.common.solution.entity.UserRole;
import com.ming.common.solution.service.LoginService;
import me.jiangcai.lib.test.SpringWebTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 获取用户
 * 新增用户，修改用户，删除用户
 *
 * @author CJ
 */
@ContextConfiguration(classes = {TestCoreConfig.class, CoreConfig.class})
@WebAppConfiguration
public class ManageLoginControllerTest extends SpringWebTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LoginService loginService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test(expected = UsernameNotFoundException.class)
    public void delete() throws Exception {
        final String name = randomMobile();
        User user = loginService.newUser(name, UUID.randomUUID().toString(), UserRole.developer);
//        mockMvc.perform(
//                post
//        )
        mockMvc.perform(
                delete("/users/{id}", user.getId())
        )
                .andExpect(status().isNoContent());

        loginService.loadUserByUsername(name);
    }

    @Test
    public void add() throws Exception {
        Map<String, Object> data = new HashMap<>();
        final String rawPassword = UUID.randomUUID().toString();
        data.put("rawPassword", rawPassword);
        final String name = randomMobile();
        data.put("username", name);
        final UserRole role = randomArray(UserRole.values(), 1)[0];
        data.put("role", role);
        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(data))
        )
                .andExpect(status().isCreated());
        //确认其的存在，并且可验证它的密码

        assertThat(passwordEncoder.matches(
                rawPassword
                , loginService.loadUserByUsername(name)
                        .getPassword()
        ))
                .as("该用户需存在并且密码要对的上")
                .isTrue();

        User user = (User) loginService.loadUserByUsername(name);
        assertThat(user.getRole())
                .as("新增的角色得屁配得上它的role")
                .isEqualByComparingTo(role);
    }

    @Test
    public void list() throws Exception {
        mockMvc.perform(
                get("/users")
        ).andDo(print());
    }

}