package com.ming.common.solution.controller

import com.ming.common.solution.service.DeployService
import me.jiangcai.lib.sys.service.SystemStringService
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

/**
 * @author CJ
 */
@Controller
open class AliHook(
        private val systemStringService: SystemStringService
        , private val deployService: DeployService
) {
    companion object {
        const val AllowKey: String = "com.ming.cs.aliHook.allowKeys"
    }

    @PostMapping("/aliImageRegisterPush")
    @ResponseBody
    @Transactional(readOnly = true)
    open fun aliImageRegisterPush(@RequestParam("key") key: String, @RequestBody body: Map<String, Any>): String {
        val data = systemStringService.getCustomSystemString(AllowKey, null, true, java.lang.String::class.java
                , null)
                ?: throw IllegalStateException("无法在尚未设置key的情况下工作")
        if (!data.toString().split(",").stream()
                        .anyMatch { input -> input == key })
            throw IllegalStateException("bad access")

        val pushData: Map<*, *> = body["push_data"] as Map<*, *>
        val repository: Map<*, *> = body["repository"] as Map<*, *>

        val region = repository["region"]
        val namespace = repository["namespace"]
        val name = repository["name"]
        val version = pushData["tag"]

        val image = deployService.findImage(region.toString(), namespace.toString(), name.toString())
                ?: return "no image"
        deployService.imageUpdate(image, version.toString())
        return "ok"
    }
}