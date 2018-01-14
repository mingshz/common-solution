package com.ming.tools.entrance.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;

/**
 * 将入口代理image push至registry
 *
 * @author CJ
 */
@Mojo(
        name = "push",
        defaultPhase = LifecyclePhase.DEPLOY
)
public class PushMojo extends AbstractEntranceMojo {
    @Parameter(property = "docker.push.registry")
    private String pushRegistry;
    @Parameter(property = "docker.username")
    private String username;

    @Parameter(property = "docker.skip.push", defaultValue = "false")
    private boolean skipPush;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // 执行tag和push
        if (skipPush)
            return;
        String prefix;
        if (pushRegistry != null && pushRegistry.length() > 0)
            prefix = pushRegistry + "/";
        else if (username != null && username.length() > 0)
            prefix = username + "/";
        else
            throw new MojoFailureException("docker.push.registry or docker.username must be setup.");

        try {
            int exist = new ProcessBuilder()
                    .command("docker", "tag", getName() + ":" + getVersion(), prefix + getName() + ":" + getVersion())
                    .inheritIO()
                    .start()
                    .waitFor();
            if (exist != 0)
                throw new MojoExecutionException("tag exit code:" + exist);
            exist = new ProcessBuilder()
                    .command("docker", "push", prefix + getName() + ":" + getVersion())
                    .inheritIO()
                    .start()
                    .waitFor();
            if (exist != 0)
                throw new MojoExecutionException("push exit code:" + exist);
        } catch (InterruptedException | IOException e) {
            throw new MojoExecutionException("", e);
        }
    }

}
