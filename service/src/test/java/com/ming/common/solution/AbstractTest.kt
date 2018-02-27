package com.ming.common.solution

import com.ming.common.solution.config.SecurityConfig
import com.ming.common.solution.entity.ImageRegister
import com.ming.common.solution.entity.User
import com.ming.common.solution.entity.UserRole
import com.ming.common.solution.repository.UserRepository
import com.ming.common.solution.service.ImageService
import com.ming.common.solution.service.ProjectService
import com.ming.common.solution.service.RuntimeEnvironmentService
import me.jiangcai.lib.test.SpringWebTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import java.util.*

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
    @Autowired
    private lateinit var projectService: ProjectService
    @Autowired
    private lateinit var userRepository: UserRepository

    /**
     * 建立一个模拟环境 允许自动跟踪shopping-beauty-client
     */
    protected fun createDemoEnv(): ImageRegister {
        val project = projectService.newProject(UUID.randomUUID().toString().substring(0, 30), null
                , "master", null)

        // 增加一个相关人士 就我自己吧
        var user = User()
        user.isEnabled = true
        user.role = UserRole.root
        user.username = UUID.randomUUID().toString()
        user.emailAddress = "caijiang@mingshz.com"
        user = userRepository.save(user)

        projectService.addRelate(project, user)

        val image = imageService?.addImage(
                "cn-shanghai", "mingshz", "shopping-beauty-client", "CJ@mingshz"
                , "Ilovemj1@docker"
        )
        val service = image?.let { runtimeEnvironmentService?.addService(project, it, "front") }
        runtimeEnvironmentService?.addHosts(ClassPathResource("hosts").file.absolutePath)
        val host = runtimeEnvironmentService?.getHost("118.178.57.117")

        val env = host?.let {
            runtimeEnvironmentService?.addRuntimeEnvironment(
                    project, it, "测试", "sb_test"
            )
        }
        env?.let { service?.let { it1 -> runtimeEnvironmentService?.updateServiceVersion(it, it1, "latest") } }

        return image!!
    }

}