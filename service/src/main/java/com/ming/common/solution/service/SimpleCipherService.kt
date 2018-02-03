package com.ming.common.solution.service

/**
 * 简单加密服务
 * @author CJ
 */
interface SimpleCipherService {
    fun encrypt(input: ByteArray): ByteArray
    fun decrypt(input: ByteArray): ByteArray
    fun encrypt(input: String): ByteArray
    fun decrypt2String(input: ByteArray): String
}