package com.ming.common.solution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.common.solution.entity.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CJ
 */
@Controller
public class LoginController {
    private static final Log log = LogFactory.getLog(LoginController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/loginStatus")
    public void loginStatus(@AuthenticationPrincipal Object object, HttpServletResponse response) throws IOException {
        if (object instanceof String) {
            response.sendError(240);
            return;
        }

//        log.debug("current Principal:" + object);
//        if (object instanceof String) {
//            return ResponseEntity.status(410).build();
//        }
        User user = (User) object;
        Map<String, Object> data = new HashMap<>();
        data.put("loginName", user.getUsername());
        data.put("currentAuthority", "user");
        data.put("name", user.getUsername());
        data.put("avatar", "https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png");
        data.put("userId", user.getId());
        data.put("projectAuthorities", Collections.emptySet());
        data.put("notifyCount", 0);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(objectMapper.writeValueAsString(data));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(objectMapper.writeValueAsBytes(data));
            outputStream.flush();
        }
//        response.sendError(200);
    }

}
