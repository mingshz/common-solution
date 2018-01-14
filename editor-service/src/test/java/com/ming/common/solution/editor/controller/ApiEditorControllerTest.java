package com.ming.common.solution.editor.controller;

import com.ming.common.solution.TestCoreConfig;
import com.ming.common.solution.editor.EditorConfig;
import com.ming.common.solution.service.ProjectService;
import me.jiangcai.lib.test.SpringWebTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author CJ
 */
@ContextConfiguration(classes = {EditorConfig.class, TestCoreConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ApiEditorControllerTest extends SpringWebTest {

    @Autowired
    private ProjectService projectService;

    @Before
    public void g() {
        projectService.deleteProject("demo");
        projectService.newProject("demo");
    }

    @Test
    public void readYaml() throws Exception {
        mockMvc.perform(get("/projectApiYaml/demo/master"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.yaml").isString())
        ;
        // 读成json
        mockMvc.perform(get("/projectApiJson/demo/master"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        // 修改它
        String newApi = UUID.randomUUID().toString();
        mockMvc.perform(put("/projectApiYaml/demo/master")
                .content(newApi)
        )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        // 现在新的api就必须正确
        mockMvc.perform(get("/projectApiYaml/demo/master"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.yaml").isString())
                .andExpect(jsonPath("$.yaml").value(newApi));

        // 看看分支
        mockMvc.perform(get("/projectBranches/demo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // 新建分支
        mockMvc.perform(post("/projectBranches/demo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"master\",\"to\":\"new-one\"}")
        )
                .andExpect(status().isOk())
        ;
        // 检查结果
        mockMvc.perform(get("/projectBranches/demo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andDo(print());

        mockMvc.perform(get("/projectApiYaml/demo/new-one"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.yaml").isString())
                .andExpect(jsonPath("$.yaml").value(newApi));
        // 尝试修改新分支
        String newApiForNewBranch = UUID.randomUUID().toString();
        mockMvc.perform(put("/projectApiYaml/demo/new-one")
                .content(newApiForNewBranch)
        )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        // 不应该影响master
        mockMvc.perform(get("/projectApiYaml/demo/master"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.yaml").isString())
                .andExpect(jsonPath("$.yaml").value(newApi));
        mockMvc.perform(get("/projectApiYaml/demo/new-one"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.yaml").isString())
                .andExpect(jsonPath("$.yaml").value(newApiForNewBranch));

    }
}