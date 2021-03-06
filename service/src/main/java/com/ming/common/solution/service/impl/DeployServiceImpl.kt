package com.ming.common.solution.service.impl

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.ming.common.solution.entity.*
import com.ming.common.solution.repository.ImageRegisterRepository
import com.ming.common.solution.service.DeployService
import com.ming.common.solution.service.SimpleCipherService
import me.jiangcai.lib.notice.*
import me.jiangcai.lib.notice.email.EmailAddress
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.activation.DataSource
import javax.persistence.EntityManager

/**
 * @author CJ
 */
@Service
class DeployServiceImpl(
        private val environment: Environment,
        private val applicationContext: ApplicationContext,
        private val imageRegisterRepository: ImageRegisterRepository,
        private val simpleCipherService: SimpleCipherService,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        private val entityManager: EntityManager,
        private val taskExecutorService: Executor,
        private val noticeService: NoticeService
) : DeployService {
    private val log = LogFactory.getLog(DeployServiceImpl::class.java)

    override fun findImage(region: String?, namespace: String, name: String): ImageRegister? {
        return imageRegisterRepository.findByRegionAndNamespaceAndName(region, namespace, name)
                ?: imageRegisterRepository.findByNamespaceAndName(namespace, name)
    }

    override fun imageUpdate(image: ImageRegister, version: String) {
        // 寻找该镜像关心的人
//        RuntimeEnvironment.class
        log.info("checking $version update for $image")
        val cb = entityManager.criteriaBuilder
        val cq = cb
                .createQuery(
                        RuntimeEnvironment::class.java
                )
        val root = cq.from(RuntimeEnvironment::class.java)
        val mapJoin = root.join(RuntimeEnvironment_.targetVersion)
        val serviceExpr = mapJoin.key()
        val versionExpr = mapJoin.value()
        cq.where(
                cb.and(
                        cb.equal(serviceExpr.get(ProjectService_.image), image)
                        , cb.equal(versionExpr, version)
                )
        )
        entityManager.createQuery(cq.distinct(true))
                .resultList
                .forEach { env -> imageUpdate(env.watchService(image), version, env) }
    }

    private fun imageUpdate(service: ProjectService, version: String, env: RuntimeEnvironment) {
        taskExecutorService.execute {
            log.info("${env.name} is updating ${service.name}")
            val loader = loadCH(env)
            val session = loader.getSession(env.managerHost.managerUser, env.managerHost.host, env.managerHost.port)

            if (!env.managerHost.isStrictHostKeyChecking) {
                session.setConfig("StrictHostKeyChecking", "no")
            }

            session.connect()
            var ex: Exception? = null

            try {
                val registerHost = service.image.toHostName(env.managerHost.mode)
                // 执行 docker login
                registerHost?.let {
                    log.info("remote executing:docker login")
                    // 如果是有区域的 那一般表示私有的…………哈哈
                    loginDocker(session, it, service.image)
                }
                val registerPrefix = if (registerHost == null) "" else "$registerHost/"
                // 执行 docker pull
                log.info("remote executing:docker pull ${registerPrefix}${service.image.namespace}/${service.image.name}:$version")
                execSession(session, "docker pull ${registerPrefix}${service.image.namespace}/${service.image.name}:$version")
                // 执行 docker image
                log.info("remote executing:docker service update --force --image ${registerPrefix}${service.image.namespace}/${service.image.name}:$version ${env.stackName}_${service.name}")
                execSession(session, "docker service update --force --image ${registerPrefix}${service.image.namespace}/${service.image.name}:$version ${env.stackName}_${service.name}")
                // 执行 docker stop `docker ps|grep sb_test_manager|awk '{print $1}'`
                log.info("remote executing:docker stop `docker ps|grep ${env.stackName}_${service.name}|awk '{print \$1}'`")
                execSession(session, "docker stop `docker ps|grep ${env.stackName}_${service.name}|awk '{print \$1}'`", null, 7 * 60)
            } catch (e: Exception) {
                log.warn("remote", e)
                ex = e
            } finally {
                session.disconnect()
            }

            // 无论如何邮件都是要发的

            val project = env.project
            // 通知相关人士
            // xx的xx环境更新即将完成
            // 项目:xx的xx环境中的xx服务即将完成更新
            // 然后附加这个环境的介绍
            val content = object : Content {
                override fun signName(): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun asHtml(attachmentRefs: MutableMap<String, String>?): String {
                    if (ex != null) {
                        return failedMessage()
                    }
                    return successMessage()
                }

                private fun failedMessage(): String {
                    return """
                <h1>${project.id}的${env.name}环境更新失败</h1>
                <p>${project.description}</p>
                <h2>更新内容:${service.name}</h2>
                <p>${env.richDescription}</p>
                <h2>失败原因:</h2>
                <p>${ex?.localizedMessage}</p>
                """
                }

                private fun successMessage(): String {
                    val p1 = """
                <h1>${project.id}的${env.name}环境即将完成更新</h1>
                <p>${project.description}</p>
                <h2>更新内容:${service.name}</h2>
                <p>${env.richDescription}</p>
                """
                    val h3 = if (env.managerHost != null) {
                        "<h3>管理主机:${env.managerHost.host}</h3>"
                    } else {
                        ""
                    }
                    val p3 = if (env.stackName != null) {
                        """<p>
                    可以通过以下指令查看环境运行概要:
                <blockquote>
                    docker stack ps ${env.stackName}
                </blockquote>
                    或者通过以下指令跟随查看更新内容的日志:
                <blockquote>
                    docker logs -f `docker ps|grep ${env.stackName}_${service.name}|awk '{print ${'$'}1}'`
                </blockquote>
                """
                    } else {
                        ""
                    }
                    return p1 + h3 + p3
                }

                override fun embedAttachments(): MutableList<DataSource> = Collections.emptyList()

                override fun templateName(): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun asText(): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun asTitle(): String = "${project.id}的${env.name}环境更新即将完成"

                override fun templateParameters(): MutableMap<String, *> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun otherAttachments(): MutableList<DataSource> = Collections.emptyList()
            }

            try {
                val targetEmail = project.relates.stream()
                        .filter { !StringUtils.isEmpty(it.emailAddress) }
                        .map { EmailAddress(it.username, it.emailAddress) }
                        .collect(Collectors.toSet())
                if (targetEmail.isEmpty())
                    return@execute
                noticeService.send({
                    try {
                        // 把它需要的属性给整出来。
                        val ps = Properties()
                        ps.addFromEnvironment(environment, "smtp.host")
                        ps.addFromEnvironment(environment, "smtp.sslPort")
                        ps.addFromEnvironment(environment, "smtp.port")
                        ps.addFromEnvironment(environment, "smtp.username")
                        ps.addFromEnvironment(environment, "smtp.password")
                        ps.addFromEnvironment(environment, "from.name")
                        ps.addFromEnvironment(environment, "from.email")

                        applicationContext.getBean(EmailNoticeSupplier::class.java, ps)
                    } catch (e: Throwable) {
                        object : NoticeSupplier {
                            override fun statusReport() {
                                TODO("Not yet implemented")
                            }

                            override fun send(to: To?, content: Content?) {
                                log.warn("send message use NOOP.")
                            }

                        }
                    }
                }, object : To {
                    override fun mobilePhone(): String {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun emailTo(): MutableSet<EmailAddress> {
                        return targetEmail
                    }

                }, content)

            } catch (e: Exception) {
                log.warn("通知时错误", e)
            }
            if (ex != null) {
                throw ex
            }
        }
    }

    private fun loginDocker(session: Session, host: String, image: ImageRegister) {
        val rawPassword = simpleCipherService.decrypt2String(image.encodePassword)
        execSession(session, "docker login --username ${image.username} $host"
                , Consumer { channel ->
            run {
                val outputStream = channel.outputStream
                outputStream.write("$rawPassword\n".toByteArray(Charset.forName("ASCII")))
                outputStream.flush()
            }
        }, 30)
    }


    private fun execSession(session: Session, command: String, working: Consumer<ChannelExec>? = null
                            , timeOut: Int = 5 * 60) {
        val channel: ChannelExec = session.openChannel("exec") as ChannelExec
        channel.setPty(true)
        channel.setCommand(command)
        channel.connect()
        try {
            working?.accept(channel)

            val outputStream = channel.outputStream
            outputStream.write("exit\n".toByteArray(Charset.forName("ASCII")))
            outputStream.flush()
            var times = 0
            while (!channel.isClosed) {
                Thread.sleep(1000)
                if (timeOut < times++)
                    throw TimeoutException("command:$command execute timeout.")
            }
            if (channel.exitStatus != 0) {
//                val data = ByteArray(channel.extInputStream.available())
//                var copies = 0
//                while (data.size>copies){
//                    copies += channel.extInputStream.read(data,copies,data.size-copies)
//                }
//                val reason = String(data, Charset.forName("ASCII"))
                throw IllegalStateException("${channel.exitStatus} for $command")
            }

        } finally {
            channel.disconnect()
        }

    }

    private fun loadCH(env: RuntimeEnvironment): JSch {
        val loader = JSch()
        loader.hostKeyRepository.add(env.managerHost.toKey(), null)
        loader.addIdentity("", env.managerHost.managerPrivateKeyData, null, env.managerHost.managerPassPhrase?.toByteArray())

        return loader
    }
}

private fun Properties.addFromEnvironment(environment: Environment, key: String) {
    environment.getProperty(key)?.let {
        put(key, it)
    }
}
