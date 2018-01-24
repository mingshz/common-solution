package com.ming.common.solution.editor.service;

import com.ming.common.solution.Project;
import com.ming.common.solution.editor.model.WatchSession;
import com.ming.common.solution.entity.User;
import com.ming.common.solution.event.NewProjectEvent;
import org.springframework.context.event.EventListener;

/**
 * @author CJ
 */
public interface APIEditorService {
    @EventListener(NewProjectEvent.class)
    void onNewProject(NewProjectEvent event);

    /**
     * @param project
     * @param branch
     * @return 获取api配置
     */
    String readAPI(Project project, String branch);

    /**
     * @param user    谁干的
     * @param project 项目
     * @param branch  分支
     * @param api     api规格
     * @return 刚写入的commit id
     */
    String writeAPI(User user, Project project, String branch, String api);

    /**
     * 开始监听
     *
     * @param session
     */
    void watch(WatchSession session);

    /**
     * 关闭监听
     *
     * @param id
     */
    void closeWatch(String id);
}
