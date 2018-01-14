package com.ming.tools.entrance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CJ
 */
public class Builder {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {
        // 便于测试 将区域分开
//        JsonNode entrance = mapper.readTree(new File("./entrance.json"));
        try (final FileInputStream fileInputStream = new FileInputStream("./entrance.json")) {
            Entrance entrance = readFromJson(fileInputStream);

            new Builder().work(new File("/"), entrance);

        }

    }

    private static Entrance readFromJson(InputStream inputStream) throws IOException {
        JsonNode root = mapper.readTree(inputStream);
        return readFromNode(root);
    }

    public static Entrance readFromNode(JsonNode root) {
        return new Entrance() {
            @Override
            public String getName() {
                JsonNode name = root.get("name");
                return name != null ? name.asText() : null;
            }

            @Override
            public String getVersion() {
                JsonNode name = root.get("version");
                return name != null ? name.asText() : null;
            }

            @Override
            public boolean isSsl() {
                JsonNode name = root.get("ssl");
                return name != null && name.asBoolean();
            }

            @Override
            public List<ApiServer> getApiServers() {
                return readList("apiServers", ApiServer.class);
            }

            private <T> List<T> readList(String fieldName, Class<T> targetClass) {
                JsonNode array = root.get(fieldName);
                if (array == null)
                    return null;
                ArrayList<T> list = new ArrayList<>();
                try {
                    final ObjectReader objectReader = mapper.readerFor(targetClass);
                    for (int i = 0; i < array.size(); i++) {
                        JsonNode one = array.get(i);
                        list.add(objectReader.readValue(one));
                    }
                    return list;
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            @Override
            public List<StaticServer> getStaticServers() {
                return readList("staticServers", StaticServer.class);
            }
        };
    }

    private void work(File workingDir, Entrance entrance) throws IOException, InterruptedException {
        // 形成Entrance的结构
        System.exit(DockerFileBuilder.create()
                .forEntrance(entrance)
                .build(workingDir, workingDir));
    }

}
