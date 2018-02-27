package com.ming.common.solution.service.impl

import com.ming.common.solution.entity.*
import com.ming.common.solution.repository.HostRepository
import com.ming.common.solution.repository.ProjectServiceRepository
import com.ming.common.solution.repository.RuntimeEnvironmentRepository
import com.ming.common.solution.service.RuntimeEnvironmentService
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author CJ
 */
@Service
class RuntimeEnvironmentServiceImpl(
        private val projectServiceRepository: ProjectServiceRepository
        , private val hostRepository: HostRepository
        , private val runtimeEnvironmentRepository: RuntimeEnvironmentRepository
) : RuntimeEnvironmentService {
    private val log = LogFactory.getLog(RuntimeEnvironmentServiceImpl::class.java)
    override fun addHosts(path: String) {
        val home = Paths.get(path)
        Files.list(home).forEach { name ->
            run {
                if (Files.isDirectory(name)) {
                    val infoPath = name.resolve("info")
                    val info = Files.readAllLines(infoPath)
                    name.resolve("id_rsa").toFile().inputStream().use { s ->
                        addHost(name.toFile().name, info[2], NetworkMode.valueOf(info[0]), info[1], s)
                    }
                }
            }
        }
    }

    override fun getHost(host: String): Host {
        return hostRepository.getOne(host)
    }

    override fun updateServiceVersion(env: RuntimeEnvironment, service: ProjectService, version: String) {
        log.info("update service:${service.name} to $version in ${env.name}")
        env.targetVersion[service] = version
        runtimeEnvironmentRepository.save(env)
    }

    override fun addRuntimeEnvironment(project: Project, host: Host, name: String, stackName: String): RuntimeEnvironment {
        log.info("add stack $stackName for $name")
        val env = RuntimeEnvironment()
        env.managerHost = host
        env.stackName = stackName
        env.name = name
        env.targetVersion = HashMap()
        env.project = project
        return runtimeEnvironmentRepository.save(env)
    }

    // 直接更新某些数据
    override fun addHost(hostName: String, rsaKey: String, mode: NetworkMode, username: String
                         , privateKey: InputStream, passPhrase: String): Host {
        log.info("add or update host $hostName")
        var host = hostRepository.findOne(hostName)
        if (host == null) {
            host = Host()
            host.host = hostName
        }
        host.key = rsaKey
        host.mode = mode
        host.managerUser = username
        host.managerPrivateKeyData = StreamUtils.copyToByteArray(privateKey)
        host.managerPassPhrase = passPhrase
        if (passPhrase.isEmpty()) {
            host.managerPassPhrase = null
        }
        return hostRepository.save(host)
    }

    override fun addService(project: Project, image: ImageRegister, name: String): ProjectService {
        log.info("add service $name for $image")
        val service = ProjectService()
        service.image = image
        service.name = name
        service.project = project
        return projectServiceRepository.save(service)
    }

}