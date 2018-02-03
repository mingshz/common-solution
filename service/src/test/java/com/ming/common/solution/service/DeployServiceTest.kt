package com.ming.common.solution.service

import com.ming.common.solution.AbstractTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author CJ
 */
class DeployServiceTest() : AbstractTest() {
    @Autowired
    private var deployService: DeployService? = null

    @Test
    fun go() {
        val image = createDemoEnv();

        deployService?.imageUpdate(image, "latest")
    }
}