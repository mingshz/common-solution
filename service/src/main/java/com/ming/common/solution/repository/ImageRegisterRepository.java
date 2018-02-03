package com.ming.common.solution.repository;

import com.ming.common.solution.entity.ImageRegister;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author CJ
 */
public interface ImageRegisterRepository extends JpaRepository<ImageRegister, Long> {
    ImageRegister findByRegionAndNamespaceAndName(String region, String namespace, String name);
}
