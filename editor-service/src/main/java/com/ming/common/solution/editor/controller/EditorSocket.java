package com.ming.common.solution.editor.controller;

import com.ming.common.solution.editor.model.WatchSession;
import com.ming.common.solution.editor.service.APIEditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 每个连接持有projectId,branch
 * 连接时什么都不会发生，也不会处理任何输入；
 * 只有在分支变化时才会通知依然有效的session 最新的id
 *
 * @author CJ
 */
@Component
public class EditorSocket extends TextWebSocketHandler {

    @Autowired
    private APIEditorService apiEditorService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 按照uri
        apiEditorService.watch(new WatchSession(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        apiEditorService.closeWatch(session.getId());
    }
}
