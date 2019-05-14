package com.ming.common.solution.controller

import com.ming.common.solution.entity.AuditTarget
import com.ming.common.solution.repository.AuditTargetRepository
import me.jiangcai.lib.ee.ServletUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.math.BigDecimal
import javax.servlet.http.HttpServletRequest

/**
 * @author CJ
 */
@Controller
class AuditController(
        @Autowired
        private val auditTargetRepository: AuditTargetRepository
) {

    @PostMapping("/public/threadSafe")
    @ResponseBody
    fun forProject(request: HttpServletRequest, @RequestParam name: String, @RequestParam fingerPrint: String, @RequestParam fingerPrintType: String): BigDecimal {

        @Suppress("WhenWithOnlyElse", "UNUSED_EXPRESSION") val fp = when (fingerPrintType) {
            else -> {
                // /etc/ssh/ssh_host_ecdsa_key.pub
                fingerPrint.split(" ").subList(0, 2).joinToString(" ")
            }
        }
        // /etc/ssh/ssh_host_ecdsa_key.pub

        val newlyAudit = AuditTarget()
        newlyAudit.fingerPrint = fp
        newlyAudit.fingerPrintType = fingerPrintType
        newlyAudit.name = name
        newlyAudit.host = ServletUtils.clientIpAddress(request)

        if (auditTargetRepository.countByName(name) == 0) {
            // 如果没有找到 那就塞一个 默认0
            auditTargetRepository.save(newlyAudit)
            return BigDecimal("0")
        }

        // 如果找到了，并且对上了指纹，那就用它，并且修改下 host 跟访问时间
        return auditTargetRepository.findByName(name)
                .find { it.fingerPrint == fp }?.let {
                    it.host = newlyAudit.host
                    it.lastVisitTime = newlyAudit.lastVisitTime
                    auditTargetRepository.save(it)
                    it.refuseRate
                } ?: illegalFingerVisitResult(newlyAudit)
    }

    private fun illegalFingerVisitResult(audit: AuditTarget): BigDecimal {
        audit.refuseRate = BigDecimal("0.2")
        auditTargetRepository.save(audit)
        return audit.refuseRate
    }
}