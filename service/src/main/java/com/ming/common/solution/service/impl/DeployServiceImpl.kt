package com.ming.common.solution.service.impl

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.ming.common.solution.entity.*
import com.ming.common.solution.repository.ImageRegisterRepository
import com.ming.common.solution.service.DeployService
import com.ming.common.solution.service.SimpleCipherService
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import javax.persistence.EntityManager

/**
 * @author CJ
 */
@Service
class DeployServiceImpl(
        private val imageRegisterRepository: ImageRegisterRepository
        , private val simpleCipherService: SimpleCipherService
        , private val entityManager: EntityManager
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
        } finally {
            session.disconnect()
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
        }, 10)
    }


    private fun execSession(session: Session, command: String, working: Consumer<ChannelExec>? = null
                            , timeOut: Int = 30) {
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