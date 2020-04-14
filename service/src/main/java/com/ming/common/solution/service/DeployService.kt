package com.ming.common.solution.service

import com.ming.common.solution.entity.ImageRegister
import org.springframework.transaction.annotation.Transactional

/**
 * 负责提供部署服务
 * @author CJ
 */
interface DeployService {
    /**
     * 当得知某镜像已更新
     */
    @Transactional(readOnly = true)
    fun imageUpdate(image: ImageRegister, version: String)

    /**
     * 寻找镜像
     */
    @Transactional(readOnly = true)
    fun findImage(region: String?, namespace: String, name: String): ImageRegister?

}