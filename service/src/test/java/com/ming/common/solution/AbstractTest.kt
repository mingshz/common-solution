package com.ming.common.solution

import com.ming.common.solution.config.SecurityConfig
import com.ming.common.solution.entity.ImageRegister
import com.ming.common.solution.entity.NetworkMode
import com.ming.common.solution.service.ImageService
import com.ming.common.solution.service.RuntimeEnvironmentService
import me.jiangcai.lib.test.SpringWebTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration

/**
 * @author CJ
 */
@ContextConfiguration(classes = arrayOf(TestCoreConfig::class, SecurityConfig::class))
@WebAppConfiguration
abstract class AbstractTest : SpringWebTest() {
    @Autowired
    private var imageService: ImageService? = null
    @Autowired
    private var runtimeEnvironmentService: RuntimeEnvironmentService? = null

    /**
     * 建立一个模拟环境 允许自动跟踪shopping-beauty-client
     */
    protected fun createDemoEnv(): ImageRegister {

        val image = imageService?.addImage(
                "cn-shanghai", "mingshz", "shopping-beauty-client", "CJ@mingshz"
                , "Ilovemj1@docker"
        )
        val service = image?.let { runtimeEnvironmentService?.addService(it, "front") }
        val host = runtimeEnvironmentService?.addHost(
                "118.178.57.117"
                , "AAAAB3NzaC1yc2EAAAADAQABAAABAQDetrqARBRXZL2gZbgja8uUg+kRc2quaNLhaZqLafrsn8OwyA1S8qlDoj5H9AfAlIoONjvzHr4Wm4j2fHHrgk1mwVHJ8BWFN4ZFBFduQ5C+Vz2+v5tBL29gt7S32rD36BgG4GD7WkSaLn0j2ECR/1PQPc+tvK0RrTNfH5KeWp9f85tqUbcYjvPZ2gFLpjQkLfW5Njfh1v3mkD3oQOlVZ+ltZZCrEO6r7Lcktz8DRsISo8o9rbFkEFqHtJoDOKUK8++/KgJ0O/07WcLoT8QtHpvI0ll28QwB0Py0K03snX4VDlcP0Lyo1jtk/iBUIjIWYlZ3hFiGQ5Rez/UgDupS1K2H"
                , NetworkMode.classics, "demo", ClassPathResource("/id_rsa").inputStream
        )

        val env = host?.let {
            runtimeEnvironmentService?.addRuntimeEnvironment(
                    it, "测试", "sb_test"
            )
        }
        env?.let { service?.let { it1 -> runtimeEnvironmentService?.updateServiceVersion(it, it1, "latest") } }

        return image!!
    }

}