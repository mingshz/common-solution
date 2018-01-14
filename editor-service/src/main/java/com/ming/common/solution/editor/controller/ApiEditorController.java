package com.ming.common.solution.editor.controller;

import com.ming.common.solution.Project;
import com.ming.common.solution.editor.service.APIEditorService;
import com.ming.common.solution.entity.UserRole;
import com.ming.common.solution.service.ProjectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CJ
 */
@Controller
public class ApiEditorController {

    private static final Log log = LogFactory.getLog(ApiEditorController.class);
    @Autowired
    private ProjectService projectService;
    @Autowired
    private APIEditorService apiEditorService;

    @PostMapping("/projectBranches/{id}")
    @PreAuthorize("hasAnyRole('" + UserRole.ROLE_USER + "','ROOT')")
    @ResponseStatus(HttpStatus.OK)
    public void addBranch(@PathVariable String id, @RequestBody Map<String, String> data) {
        log.debug(data);
        projectService.newBranch(id, data.get("from"), data.get("to"));
    }

    @GetMapping("/projectBranches/{id}")
    @ResponseBody
    public String[] getBranches(@PathVariable String id) {
//        Project project = projectService.byId(id);
        return projectService.branches(id);
    }

    @GetMapping("/projectApiYaml/{id}/{branch}")
    @ResponseBody
    public Map readYaml(@PathVariable String id, @PathVariable String branch) {
        Project project = projectService.byId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", projectService.commitId(id, branch));
        data.put("yaml", apiEditorService.readAPI(project, branch));
        return data;
    }

    @GetMapping("/projectApiJson/{id}/{branch}")
    @ResponseBody
    public Object readJson(@PathVariable String id, @PathVariable String branch) {
        Project project = projectService.byId(id);
        String yaml = apiEditorService.readAPI(project, branch);
        return new Yaml().load(yaml);
    }

    @PutMapping("/projectApiYaml/{id}/{branch}")
    @PreAuthorize("hasAnyRole('" + UserRole.ROLE_USER + "','ROOT')")
    public ResponseEntity<String> writeYaml(@PathVariable String id, @PathVariable String branch
            , @RequestBody String api) {
        Project project = projectService.byId(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(
                        apiEditorService.writeAPI(project, branch, api)
                );
    }

}
