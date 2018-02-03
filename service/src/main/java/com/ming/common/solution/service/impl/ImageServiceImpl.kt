package com.ming.common.solution.service.impl

import com.ming.common.solution.entity.ImageRegister
import com.ming.common.solution.repository.ImageRegisterRepository
import com.ming.common.solution.service.ImageService
import com.ming.common.solution.service.SimpleCipherService
import org.springframework.stereotype.Service

/**
 * @author CJ
 */
@Service
class ImageServiceImpl(
        private val imageRegisterRepository: ImageRegisterRepository
        , private val simpleCipherService: SimpleCipherService
) : ImageService {
    override fun addImage(region: String, namespace: String, name: String, username: String, rawPassword: String)
            : ImageRegister {
        val image = ImageRegister()
        image.username = username
        image.namespace = namespace
        image.region = region
        image.name = name
        image.encodePassword = simpleCipherService.encrypt(rawPassword)
        return imageRegisterRepository.save(image)
    }
}