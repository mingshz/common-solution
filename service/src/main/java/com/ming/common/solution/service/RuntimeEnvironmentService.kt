package com.ming.common.solution.service

import com.ming.common.solution.entity.*
import com.ming.common.solution.entity.ProjectService
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream

/**
 * 运行环境相关服务
 * @author CJ
 */
interface RuntimeEnvironmentService {

    /**
     * 就这个镜像添加一个服务
     */
    @Transactional
    fun addService(project: Project, image: ImageRegister, name: String): ProjectService

    /**
     * 添加hostKEY
     * @param hostName 主机
     * @param rsaKey ssh-rsa 指纹；可以从~/known_hosts 中获取
     * @param mode 网络模式
     * @param username 登录名
     * @param privateKey 密钥文件
     */
    @Transactional
    fun addHost(hostName: String, rsaKey: String, mode: NetworkMode, username: String, privateKey: InputStream, passPhrase: String = ""): Host

    /**
     * 添加一个环境
     * @param host 管控主机
     * @param name 名称
     * @param stackName stack name
     */
    @Transactional
    fun addRuntimeEnvironment(project: Project, host: Host, name: String, stackName: String): RuntimeEnvironment

    @Transactional
    fun updateServiceVersion(env: RuntimeEnvironment, service: ProjectService, version: String)

    /**
     * 从特定path下扫描把扫描到的内容都添加到系统中
     */
    @Transactional
    fun addHosts(path: String)

    /**
     * 读取一个主机
     */
    @Transactional(readOnly = true)
    fun getHost(host: String): Host

}