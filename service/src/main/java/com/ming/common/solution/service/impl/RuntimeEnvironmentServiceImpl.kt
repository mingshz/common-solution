package com.ming.common.solution.service.impl

import com.ming.common.solution.entity.*
import com.ming.common.solution.repository.HostRepository
import com.ming.common.solution.repository.ProjectServiceRepository
import com.ming.common.solution.repository.RuntimeEnvironmentRepository
import com.ming.common.solution.service.RuntimeEnvironmentService
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import java.io.InputStream

/**
 * @author CJ
 */
@Service
class RuntimeEnvironmentServiceImpl(
        private val projectServiceRepository: ProjectServiceRepository
        , private val hostRepository: HostRepository
        , private val runtimeEnvironmentRepository: RuntimeEnvironmentRepository
) : RuntimeEnvironmentService {

    override fun updateServiceVersion(env: RuntimeEnvironment, service: ProjectService, version: String) {
        env.targetVersion[service] = version
        runtimeEnvironmentRepository.save(env)
    }

    override fun addRuntimeEnvironment(host: Host, name: String, stackName: String): RuntimeEnvironment {
        val env = RuntimeEnvironment()
        env.managerHost = host
        env.stackName = stackName
        env.name = name
        env.targetVersion = HashMap()
        return runtimeEnvironmentRepository.save(env)
    }

    // 直接更新某些数据
    override fun addHost(hostName: String, rsaKey: String, mode: NetworkMode, username: String
                         , privateKey: InputStream, passPhrase: String): Host {
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

    override fun addService(image: ImageRegister, name: String): ProjectService {
        val service = ProjectService()
        service.image = image
        service.name = name
        return projectServiceRepository.save(service)
    }

}