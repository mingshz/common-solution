package com.ming.common.solution.service.impl

import com.ming.common.solution.service.SimpleCipherService
import org.apache.commons.codec.binary.Hex
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * @author CJ
 */
@Service
class SimpleCipherServiceImpl(
        private val environment: Environment
) : SimpleCipherService {

    private fun toKey(): SecretKey {
        return SecretKeySpec(
                Hex.decodeHex(environment.getProperty("desKey", "0102030405060708").toCharArray())
                , "DES")
    }

    private fun toCipher(): Cipher {
        return Cipher.getInstance("DES/ECB/PKCS5Padding")
    }

    override fun encrypt(input: String): ByteArray {
        return encrypt(input.toByteArray())
    }

    override fun encrypt(input: ByteArray): ByteArray {
        val cipher = toCipher()
        cipher.init(Cipher.ENCRYPT_MODE, toKey())
        return cipher.doFinal(input)
    }

    override fun decrypt2String(input: ByteArray): String {
        return String(decrypt(input))
    }

    override fun decrypt(input: ByteArray): ByteArray {
        val cipher = toCipher()
        cipher.init(Cipher.DECRYPT_MODE, toKey())
        return cipher.doFinal(input)
    }

}