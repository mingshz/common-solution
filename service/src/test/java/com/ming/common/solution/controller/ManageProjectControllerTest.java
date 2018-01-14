package com.ming.common.solution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.common.solution.TestCoreConfig;
import com.ming.common.solution.config.CoreConfig;
import com.ming.common.solution.service.ProjectService;
import me.jiangcai.lib.test.SpringWebTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author CJ
 */
@ContextConfiguration(classes = {TestCoreConfig.class, CoreConfig.class})
@WebAppConfiguration
public class ManageProjectControllerTest extends SpringWebTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ProjectService projectService;

    @Test
    public void flow() throws Exception {
        Map<String, Object> data = new HashMap<>();
        final String projectId = randomMobile();
        data.put("id", projectId);
        final String description = randomMobile();
        data.put("description", description);
        mockMvc.perform(
                post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(data))
        )
                .andExpect(status().isCreated());

        projectService.byId(projectId);

        mockMvc.perform(
                get("/projects")
        )
                .andDo(print());
    }

}