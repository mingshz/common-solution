package com.ming.common.solution.controller;

import com.ming.common.solution.entity.Project;
import com.ming.common.solution.entity.Project_;
import com.ming.common.solution.service.ProjectService;
import com.ming.common.solution.service.SystemService;
import me.jiangcai.crud.controller.AbstractCrudController;
import me.jiangcai.crud.row.FieldDefinition;
import me.jiangcai.crud.row.field.FieldBuilder;
import me.jiangcai.crud.row.field.Fields;
import me.jiangcai.lib.jpa.JpaFunctionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author CJ
 */
@Controller
@RequestMapping("/projects")
//@RowCustom(distinct = true, dramatizer = AntDesignPaginationDramatizer.class)
public class ManageProjectController extends AbstractCrudController<Project, String, Project> {

    @Autowired
    private SystemService systemService;
    @Autowired
    private ProjectService projectService;

    @Override
    protected Project preparePersist(Project data, WebRequest otherData) {
        super.preparePersist(data, otherData);
        // 先检查下有没有啊
        try {
            projectService.byId(data.getId());
            // 找到了
            throw new IllegalStateException("这个id已经存在了呢");
        } catch (IllegalArgumentException ignored) {
        }
        if (data.getBranch() == null)
            data.setBranch("master");
        return data;
    }

    @Override
    protected void postPersist(Project entity) {
        super.postPersist(entity);
        projectService.newProject(entity.getId());
    }

    @Override
    protected List<FieldDefinition<Project>> listFields() {
        return Arrays.asList(
                Fields.asBasic("id"),
                Fields.asBasic("description"),
                Fields.asBasic("branch"),
                Fields.asBasic("avatar"),
                FieldBuilder.asName(Project.class, "editorUrl")
                        .addBiSelect((projectRoot, criteriaBuilder) -> JpaFunctionUtils.contact(criteriaBuilder
                                , criteriaBuilder.literal("/editor/")
                                , projectRoot.get(Project_.id)
                                , criteriaBuilder.literal("/")
                                , projectRoot.get(Project_.branch)
                        ))
                        .addFormat((data, type) -> systemService.toUrl(data.toString()))
                        .build()
        );
    }

    @Override
    protected Specification<Project> listSpecification(Map<String, Object> queryData) {
        return null;
    }
}
