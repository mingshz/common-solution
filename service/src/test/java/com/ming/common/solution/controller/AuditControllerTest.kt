package com.ming.common.solution.controller

import com.ming.common.solution.TestCoreConfig
import com.ming.common.solution.config.SecurityConfig
import com.ming.common.solution.entity.AuditTarget
import com.ming.common.solution.repository.AuditTargetRepository
import me.jiangcai.lib.test.SpringWebTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author CJ
 */
@ContextConfiguration(classes = [TestCoreConfig::class, SecurityConfig::class])
@WebAppConfiguration
class AuditControllerTest : SpringWebTest() {

    @Autowired
    private lateinit var auditTargetRepository: AuditTargetRepository


    @Test
    fun go() {

        val name = randomMobile()

        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "/etc/ssh/ssh_host_ecdsa_key.pub")
                        .param("name", name)
                        .param("fingerPrint", "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMA1nB30O4HopMGZf8kvBDLETa61xkf+aFNhwXHmp8KYdXTEZJfvMRmbEGC+PO9qSIGd2SQF23Vbu7uqHOqNWjA= root@iZbp1c348iysx12f9s0edqZ")
        )
                .andExpect(status().isOk)
                .andExpect(content().string("0.2"))

        val at = AuditTarget()
        at.name = name
//        at.refuseRate
        val at2 = auditTargetRepository.save(at)

        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "/etc/ssh/ssh_host_ecdsa_key.pub")
                        .param("name", name)
                        .param("fingerPrint", "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMA1nB30O4HopMGZf8kvBDLETa61xkf+aFNhwXHmp8KYdXTEZJfvMRmbEGC+PO9qSIGd2SQF23Vbu7uqHOqNWjA= root@iZbp1c348iysx12f9s0edqZ")
        )
                .andExpect(status().isOk)
                .andExpect(content().string("0.2"))

        at2.fingerPrint = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMA1nB30O4HopMGZf8kvBDLETa61xkf+aFNhwXHmp8KYdXTEZJfvMRmbEGC+PO9qSIGd2SQF23Vbu7uqHOqNWjA="
        auditTargetRepository.save(at2)


        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "/etc/ssh/ssh_host_ecdsa_key.pub")
                        .param("name", name)
                        .param("fingerPrint", "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMA1nB30O4HopMGZf8kvBDLETa61xkf+aFNhwXHmp8KYdXTEZJfvMRmbEGC+PO9qSIGd2SQF23Vbu7uqHOqNWjA= root@iZbp1c348iysx12f9s0edqZ")
        )
                .andExpect(status().isOk)
                .andExpect(content().string("0"))
    }

}