package com.ming.tools.entrance.maven;

import com.ming.tools.entrance.DockerFileBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 将项目入口代理打包为一个image在本地。
 *
 * @author CJ
 */
@Mojo(
        name = "build",
        defaultPhase = LifecyclePhase.INSTALL
)
public class BuildMojo extends AbstractEntranceMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // 完成构建
        // 首先设置运行目录
        // 并在目录生成json 接下来一切都在这个目录中完成
        Path workingDir = getProject().getBasedir().toPath().resolve("target").resolve("entrance");
//        File workingDir = new File(new File(project.getBasedir(), "target"), "entrance");


        try {
            assert Files.exists(workingDir) || Files.createDirectories(workingDir.resolve("every")) != null;
            getLog().info("working in " + workingDir);
            DockerFileBuilder.create()
                    .forEntrance(this)
                    .build(getProject().getBasedir(), workingDir.toFile());
        } catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException("IO", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("STOPPED", e);
        }
    }
}
