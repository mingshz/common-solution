package com.ming.common.solution.repository;

import com.ming.common.solution.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author CJ
 */
public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {
}
