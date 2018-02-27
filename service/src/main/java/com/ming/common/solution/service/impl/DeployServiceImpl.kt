package com.ming.common.solution.service.impl

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.ming.common.solution.entity.*
import com.ming.common.solution.repository.ImageRegisterRepository
import com.ming.common.solution.service.DeployService
import com.ming.common.solution.service.SimpleCipherService
import me.jiangcai.lib.notice.Content
import me.jiangcai.lib.notice.NoticeService
import me.jiangcai.lib.notice.To
import me.jiangcai.lib.notice.email.EmailAddress
import org.apache.commons.logging.LogFactory
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
        private val imageRegisterRepository: ImageRegisterRepository
        , private val simpleCipherService: SimpleCipherService
        , private val entityManager: EntityManager
        , private val taskExecutorService: Executor
        , private val noticeService: NoticeService
) : DeployService {
    private val log = LogFactory.getLog(DeployServiceImpl::class.java)

    override fun findImage(region: String, namespace: String, name: String): ImageRegister? {
        return imageRegisterRepository.findByRegionAndNamespaceAndName(region, namespace, name)
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
            val session = loader.getSession(env.managerHost.managerUser, env.managerHost.host)

            session.connect()

            try {
                val registerHost = service.image.toHostName(env.managerHost.mode)
                // 执行 docker login
                loginDocker(session, registerHost, service.image)
                // 执行 docker pull
                execSession(session, "docker pull $registerHost/${service.image.namespace}/${service.image.name}:$version")
                // 执行 docker image
                execSession(session, "docker service update --force --image $registerHost/${service.image.namespace}/${service.image.name}:$version ${env.stackName}_${service.name}")
                // 执行 docker stop `docker ps|grep sb_test_manager|awk '{print $1}'`
                execSession(session, "docker stop `docker ps|grep ${env.stackName}_${service.name}|awk '{print \$1}'`", null, 7 * 60)
            } finally {
                session.disconnect()
            }

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
                    val h1 = "<h1>${project.id}的${env.name}环境即将完成更新</h1>"
                    val p1 = "<p>${project.description}</p>"
                    val h2 = "<h2>更新内容:${service.name}</h2>"
                    val p2 = "<p>${env.richDescription}</p>"
                    return h1 + p1 + h2 + p2
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
                noticeService.send("me.jiangcai.lib.notice.EmailNoticeSupplier", object : To {
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