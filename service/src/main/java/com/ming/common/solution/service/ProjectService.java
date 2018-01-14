package com.ming.common.solution.service;

import com.ming.common.solution.Project;

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
}
