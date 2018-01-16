package com.ming.tools.entrance;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author CJ
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ApiServer extends Endpoint implements Serializable {
    //    localApiFile: 本地api json文件
//    urlPrefix: 可选 url地址，默认是 http://cs.ming.com
//    projectId: 项目id
//    useGitBranch: 默认true 使用当前代码分支作为分支名
//    branch: 可选
    /**
     * 本地的api json 文件
     */
    @Parameter
    private String localApiFile;
    /**
     * 服务端地址
     * 默认 csm.ming.com
     */
    @Parameter(defaultValue = "csm.ming.com")
    private String apiServerHost;
    /**
     * 是否使用安全协议
     */
    @Parameter
    private boolean secure;
    /**
     * 项目id，它和{@link #localApiFile}其中一个必须被选择
     */
    @Parameter
    private String projectId;
    /**
     * 是否使用git分支作为api branch
     */
    @Parameter(defaultValue = "true")
    private boolean useGitBranch = true;
    /**
     * 明示分支
     */
    @Parameter
    private String branch;
}
