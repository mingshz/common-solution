package com.ming.common.solution.repository

import com.ming.common.solution.entity.AuditTarget
import org.springframework.data.jpa.repository.JpaRepository

/**
 * @author CJ
 */
interface AuditTargetRepository : JpaRepository<AuditTarget, Long> {
    fun countByName(name: String): Int

    fun findByName(name: String): List<AuditTarget>
}