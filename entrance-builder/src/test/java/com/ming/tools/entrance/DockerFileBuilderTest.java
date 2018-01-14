package com.ming.tools.entrance;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author CJ
 */
public class DockerFileBuilderTest extends TestBase {

    @Test
    public void build1() throws IOException, InterruptedException {
        File working = new File("target");
        DockerFileBuilder.create()
                .forEntrance(normalEntrance())
                .build(null, working);
    }

    @Test
    public void build2() throws IOException, InterruptedException {
        File working = new File("target");
        DockerFileBuilder.create()
                .forEntrance(normalWithFrontEndsEntrance())
                .build(null, working);
    }

    @Test
    public void build3() throws IOException, InterruptedException {
        File working = new File("target");
        // 先把证书拷贝过去
        copyCertificate(working);

        DockerFileBuilder.create()
                .forEntrance(normalWithSSLEntrance())
                .build(null, working);
    }

    private void copyCertificate(File dir) throws IOException {
        Path dirPath = Paths.get(dir.toURI()).resolve("certificate");
        // 如果不存在则建立目录
        if (!Files.exists(dirPath))
            Files.createDirectories(dirPath);

        final Path keyPath = dirPath.resolve("host.key");
        if (!Files.exists(keyPath))
            try (InputStream inputStream = new ClassPathResource("/host.key").getInputStream()) {
                Files.copy(inputStream, keyPath);
            }
        final Path pemPath = dirPath.resolve("host.pem");
        if (!Files.exists(pemPath))
            try (InputStream inputStream = new ClassPathResource("/host.pem").getInputStream()) {
                Files.copy(inputStream, pemPath);
            }
    }

}