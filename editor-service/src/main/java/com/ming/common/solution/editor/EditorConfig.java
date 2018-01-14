package com.ming.common.solution.editor;

import com.ming.common.solution.config.CoreConfig;
import com.ming.common.solution.editor.controller.EditorSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 编辑器
 * 目前只有api编辑器
 *
 * @author CJ
 */
@Configuration
@Import(CoreConfig.class)
@ComponentScan({
        "com.ming.common.solution.editor.controller"
        , "com.ming.common.solution.editor.service"
})
@EnableWebMvc
@EnableWebSocket
public class EditorConfig implements WebSocketConfigurer {
    @Autowired
    private EditorSocket editorSocket;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(editorSocket, "/watchProjectApi/{id}/{branch}")
                .setAllowedOrigins("*");
    }
}
