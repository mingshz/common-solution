package com.ming.common.solution.controller

import com.ming.common.solution.TestCoreConfig
import com.ming.common.solution.config.SecurityConfig
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
        auditTargetRepository.deleteAll()

        val name = randomMobile()
        val finger1 = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMA1nB30O4HopMGZf8kvBDLETa61xkf+aFNhwXHmp8KYdXTEZJfvMRmbEGC+PO9qSIGd2SQF23Vbu7uqHOqNWjA= root@iZbp1c348iysx12f9s0edqZ"

        // 一个新设备的访问，那么肯定会告诉我一个很好的结果
        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "hostname")
                        .param("name", randomMobile())
                        .param("fingerPrint", "localhost")
        )
                .andExpect(status().isOk)
                .andExpect(content().string("0"))

        // 一个新设备的访问，那么肯定会告诉我一个很好的结果
        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "/etc/ssh/ssh_host_ecdsa_key.pub")
                        .param("name", name)
                        .param("fingerPrint", finger1)
        )
                .andExpect(status().isOk)
                .andExpect(content().string("0"))

        // 如果更改了签名，自然就比较糟糕了。
        val finger2 = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMA1nB30O4HopMGZf8kvBDLEXa61xkf+aFNhwXHmp8KYdXTEZJfvMRmbEGC+PO9qSIGd2SQF23Vbu7uqHOqNWjA= root@iZbp1c348iysx12f9s0edqZ"

        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "/etc/ssh/ssh_host_ecdsa_key.pub")
                        .param("name", name)
                        .param("fingerPrint", finger2)
        )
                .andExpect(status().isOk)
                .andExpect(content().string(AuditController.IllegalRefuseRate))

        // 但是可以确保之前的继续ok
        mockMvc.perform(
                post("/public/threadSafe")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fingerPrintType", "/etc/ssh/ssh_host_ecdsa_key.pub")
                        .param("name", name)
                        .param("fingerPrint", finger1)
        )
                .andExpect(status().isOk)
                .andExpect(content().string("0"))
    }

}