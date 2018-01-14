package com.ming.common.solution.editor.model;

import com.ming.common.solution.Project;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CJ
 */
@Data
public class WatchSession {
    private static final Log log = LogFactory.getLog(WatchSession.class);
    private final WebSocketSession session;
    private Pattern pathPattern = Pattern.compile(".*/([a-zA-Z0-9-]{3,})/([a-zA-Z0-9-_]+)");

    /**
     * @param project
     * @param branch
     * @return 是否符合该项目
     */
    public boolean match(Project project, String branch) {
        // [a-zA-Z0-9-]{3,}
        // 类似的就是branch
        Matcher matcher = pathPattern.matcher(session.getUri().getPath());
        return matcher.matches() && matcher.group(1).equalsIgnoreCase(project.getId()) && matcher.group(2).equalsIgnoreCase(branch);
    }

    public void sendLastCommitId(String commitId) {
        try {
            session.sendMessage(new TextMessage(commitId));
        } catch (Throwable e) {
            log.warn("dispatch " + commitId, e);
        }
    }
}
