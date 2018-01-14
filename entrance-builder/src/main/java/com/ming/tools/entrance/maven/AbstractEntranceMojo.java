package com.ming.tools.entrance.maven;

import com.ming.tools.entrance.ApiServer;
import com.ming.tools.entrance.Entrance;
import com.ming.tools.entrance.StaticServer;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

/**
 * @author CJ
 */
abstract class AbstractEntranceMojo extends AbstractMojo implements Entrance {

    @Setter
    @Getter
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * 构建image的名称
     */
    @Setter
    @Getter
    @Parameter(defaultValue = "${project.artifactId}-entrance")
    private String name;
    /**
     * 版本，默认就是maven project version
     */
    @Setter
    @Getter
    @Parameter(defaultValue = "${project.version}")
    private String version;
    /**
     * 是否启用安全
     * certificate 文件夹 如果开启了ssl的话，里面包含的文件都取名为host;比如 host.key host.pem
     */
    @Setter
    @Getter
    @Parameter
    private boolean ssl;

    /**
     * 支持使用api-mocker的服务器列表
     */
    @Setter
    @Getter
    @Parameter
    private List<ApiServer> apiServers;
    /**
     * 静态服务器列表
     */
    @Setter
    @Getter
    @Parameter
    private List<StaticServer> staticServers;

}
