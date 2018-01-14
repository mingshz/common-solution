package com.ming.tools.entrance;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author CJ
 */
public class ConfigFileBuilderTest extends TestBase {

    @Test
    public void build() throws IOException {
        File file = new File("target/http.conf");
        Entrance entrance = normalEntrance();
        ConfigFileBuilder.create()
                .forEntrance(entrance)
                .build(null, file);

    }
}