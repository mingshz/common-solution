package com.ming.common.solution.event;

import com.ming.common.solution.Project;
import lombok.Data;
import org.eclipse.jgit.lib.Repository;

/**
 * 新增项目时触发
 *
 * @author CJ
 */
@Data
public class NewProjectEvent {
    private final Project project;
    private final Repository repository;
}
