package com.ming.common.solution.service.file;

import com.ming.common.solution.Project;
import lombok.Data;

import java.nio.file.Path;

/**
 * @author CJ
 */
@Data
public class FileProject implements Project {
    private final String id;
    private final Path path;
}
