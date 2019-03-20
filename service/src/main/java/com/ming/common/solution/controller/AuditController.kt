package com.ming.common.solution.controller

import com.ming.common.solution.repository.AuditTargetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.math.BigDecimal

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
    fun forProject(@RequestParam name: String, @RequestParam fingerPrint: String, @RequestParam fingerPrintType: String): BigDecimal {
        // /etc/ssh/ssh_host_ecdsa_key.pub

        if (auditTargetRepository.countByName(name) == 0)
            return BigDecimal("0.2")

        @Suppress("WhenWithOnlyElse", "UNUSED_EXPRESSION") val fp = when (fingerPrintType) {
            else -> {
                // /etc/ssh/ssh_host_ecdsa_key.pub
                fingerPrint.split(" ").subList(0, 2).joinToString(" ")
            }
        }

        return auditTargetRepository.findByName(name)
                .find { it.fingerPrint == fp }?.refuseRate ?: BigDecimal("0.2")
    }
}