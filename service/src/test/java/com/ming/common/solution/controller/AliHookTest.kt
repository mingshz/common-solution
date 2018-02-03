package com.ming.common.solution.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ming.common.solution.AbstractTest
import me.jiangcai.lib.sys.service.SystemStringService
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.ws.rs.core.MediaType

/**
 * POST /payload HTTP/1.1
 * Content-Type: application/json
 * Request URL: https://cs.console.aliyun.com/hook/trigger?triggerUrl=YzRmMWE5YzM2ZjMzYzQ0NmFiMGYzNWJlMmM2MjM2NzIyfGV4cHJlc3N8cmVkZXBsb3l8MThlMmllY2drdXYyZXw=&secret=365a4a664b45615438716a487a75695a7ac48329224b35b073c2197374e7d62a
 * Request method: POST
 * {
 * "push_data": {
 * "digest": "sha256:457f4aa83fc9a6663ab9d1b0a6e2dce25a12a943ed5bf2c1747c58d48bbb4917",
 * "pushed_at": "2016-11-29 12:25:46",
 * "tag": "latest"
 * },
 * "repository": {
 * "date_created": "2016-10-28 21:31:42",
 * "name": "repoTest",
 * "namespace": "namespace",
 * "region": "cn-hangzhou",
 * "repo_authentication_type": "NO_CERTIFIED",
 * "repo_full_name": "namespace/repoTest",
 * "repo_origin_type": "NO_CERTIFIED",
 * "repo_type": "PUBLIC"
 * }
 * }
 * @author CJ
 */
class AliHookTest : AbstractTest() {
    @Autowired
    private var systemStringService: SystemStringService? = null

    @Test
    fun go() {
        createDemoEnv()
        val data = HashMap<String, Any>()
        data["digest"] = "sha256:457f4aa83fc9a6663ab9d1b0a6e2dce25a12a943ed5bf2c1747c58d48bbb4917"
        data["pushed_at"] = "2016-11-29 12:25:46"
        data["tag"] = "latest"

        val repository = HashMap<String, Any>()
        repository["date_created"] = "2016-10-28 21:31:42"
        repository["name"] = "shopping-beauty-client"
        repository["namespace"] = "mingshz"
        repository["region"] = "cn-shanghai"

        val all = HashMap<String, Any>()
        all["push_data"] = data
        all["repository"] = repository

        // 增设一个识别服
        val randomOne = randomMobile()
        systemStringService?.updateSystemString(AliHook.AllowKey, randomOne)

        // 再执行mvc方法
        mockMvc.perform(
                post("/aliImageRegisterPush?key=$randomOne")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ObjectMapper().writeValueAsBytes(all))
        )
                .andExpect(status().isOk)
                .andExpect(content().string("ok"))


    }
}