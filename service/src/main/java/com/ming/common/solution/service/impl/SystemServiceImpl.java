package com.ming.common.solution.service.impl;

import com.ming.common.solution.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author CJ
 */
@Service
public class SystemServiceImpl implements SystemService {

    @Autowired
    private Environment environment;

    @Override
    public String toUrl(String uri) {
        return environment.getProperty("url", "http://localhost") + uri;
    }
}
