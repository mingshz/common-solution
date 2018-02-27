package com.ming.common.solution.service;

import com.ming.common.solution.Project;
import com.ming.common.solution.entity.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

/**
 * 项目相关服务
 *
 * @author CJ
 */
public interface ProjectService {

    /**
     * @param id id
     * @return 寻找特定id的项目
     * @throws IllegalArgumentException 如果找不到
     */
    Project byId(String id);

    /**
     * @param id id
     * @return 新增项目
     */
    Project newProject(String id);

    /**
     * @param id          项目id
     * @param description 描述
     * @param branch      分支
     * @param avatar      图像
     * @return 新增一个项目
     */
    @Transactional
    com.ming.common.solution.entity.Project newProject(String id, String description, String branch, String avatar);

    void deleteProject(String id);

    /**
     * @return 所有项目
     */
    Stream<Project> projectStream();

    /**
     * @param id id
     * @return 项目分支
     */
    String[] branches(String id);

    /**
     * @param id     id
     * @param branch 分支
     * @return 特定分支最后的commit id
     */
    String commitId(String id, String branch);

    /**
     * 新增分支
     *
     * @param id         id
     * @param fromBranch 起始分支
     * @param toBranch   新分支名称
     */
    void newBranch(String id, String fromBranch, String toBranch);

    @Transactional
    void addRelate(com.ming.common.solution.entity.Project project, User user);
}
