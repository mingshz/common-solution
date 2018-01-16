package com.ming.tools.entrance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * 构建docker 文件
 *
 * @author CJ
 */
public class DockerFileBuilder {
    private Entrance entrance;

    private DockerFileBuilder() {
    }

    public static DockerFileBuilder create() {
        return new DockerFileBuilder();
    }

    public DockerFileBuilder forEntrance(Entrance entrance) {
        this.entrance = entrance;
        return this;
    }

    /**
     * @param cwd        运行目录
     * @param workingDir 工作目录，也就是会生成临时文件的一些目录
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int build(File cwd, File workingDir) throws IOException, InterruptedException {
        // 首先构造 /etc/nginx/conf.d/default.conf
        if (!workingDir.exists())
            if (!workingDir.mkdirs())
                throw new IOException("failed to create folder:" + workingDir);
        ConfigFileBuilder.create()
                .forEntrance(entrance)
                .build(cwd, new File(workingDir, "default.conf"));
        // 完成之后呢 开始构建docker
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(new File(workingDir, "Dockerfile")), Charset.forName("UTF-8"))) {

            writer.write("FROM nginx:1.13\n");
            writer.write("COPY default.conf /etc/nginx/conf.d/default.conf\n");

            if (entrance.isSsl()) {
                writer.write("ADD certificate /etc/nginx/certificate\n");
                writer.write("EXPOSE 80 443\n");
            } else
                writer.write("EXPOSE 80\n");
            writer.flush();
        }

        return new ProcessBuilder()
                .directory(workingDir)
                .command("docker", "build", "-t", entrance.getName(), "-t", entrance.getName() + ":" + entrance.getVersion(), workingDir.getAbsolutePath())
                .inheritIO()
                .start()
                .waitFor()
                ;
    }
}
