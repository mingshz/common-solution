package com.ming.common.solution.repository;

import com.ming.common.solution.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author CJ
 */
public interface HostRepository extends JpaRepository<Host, String> {
}
