package com.ming.common.solution.service

import com.ming.common.solution.TestCoreConfig
import com.ming.common.solution.config.CoreConfig
import me.jiangcai.lib.test.SpringWebTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.codec.Hex
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.util.StreamUtils
import java.io.FileOutputStream
import java.util.*
import javax.crypto.KeyGenerator

/**
 * @author CJ
 */
@ContextConfiguration(classes = arrayOf(TestCoreConfig::class, CoreConfig::class))
@WebAppConfiguration
class SimpleCipherServiceTest : SpringWebTest() {
    @Autowired
    private lateinit var simpleCipherService: SimpleCipherService

    @Test
    fun go() {
        val data = UUID.randomUUID().toString()
        val after = simpleCipherService.encrypt(data)
        assertThat(
                simpleCipherService.decrypt2String(after)
        ).isEqualTo(data)

        val kg = KeyGenerator.getInstance("DES")
        val key = kg.generateKey()
        println(Hex.encode(key.encoded))
    }

    @Test
    fun toFile() {
        val out = FileOutputStream("./target/x.bin")
        out.use {
            StreamUtils.copy(simpleCipherService.encrypt("plain"), it)
            it.flush()
        }

    }
}