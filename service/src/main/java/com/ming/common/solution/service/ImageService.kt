package com.ming.common.solution.service

import com.ming.common.solution.entity.ImageRegister
import org.springframework.transaction.annotation.Transactional

/**
 * @author CJ
 */
interface ImageService {
    @Transactional
    fun addImage(region: String, namespace: String, name: String, username: String, rawPassword: String): ImageRegister
}