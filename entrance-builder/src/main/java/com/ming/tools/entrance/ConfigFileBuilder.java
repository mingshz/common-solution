package com.ming.tools.entrance;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * nginx 配置文件生成器
 *
 * @author CJ
 */
public class ConfigFileBuilder {
    private Entrance entrance;
    private StringWriter httpWriter;
    private StringWriter httpsWriter;
    private File cwd;
    private Pattern uriPattern = Pattern.compile("\\{[a-zA-Z0-9-_]+}");

    private ConfigFileBuilder() {
    }

    public static ConfigFileBuilder create() {
        return new ConfigFileBuilder();
    }

    public ConfigFileBuilder forEntrance(Entrance entrance) {
        this.entrance = entrance;
        return this;
    }

    /**
     * @param cwd  可选的运行目录
     * @param file 目标文件
     * @throws IOException
     */
    public void build(File cwd, File file) throws IOException {
        this.cwd = cwd;
        httpWriter = new StringWriter();
        if (entrance.isSsl()) {
            httpsWriter = new StringWriter();
        }
        start();// server {
        port();
        name();//    server_name  localhost;
        // ssl only
        writeHttps("\tssl on;\n");
        // 实际目录应该是  /etc/nginx/certificate
        writeHttps("\tssl_certificate   certificate/host.pem;\n" +
                "\tssl_certificate_key  certificate/host.key;\n" +
                "\tssl_session_timeout 5m;\n" +
                "\tssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;\n" +
                "\tssl_protocols TLSv1 TLSv1.1 TLSv1.2;\n" +
                "\tssl_prefer_server_ciphers on;\n");
        servers();
        frontEnds();
        end();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"))) {
            if (httpWriter != null)
                writer.write(httpWriter.toString());
            if (httpsWriter != null)
                writer.write(httpsWriter.toString());
            writer.flush();
        }
    }

    private void frontEnds() throws IOException {
        final List<StaticServer> servers = entrance.getStaticServers();
        if (servers != null) {
            for (StaticServer server : servers) {
                frontEnd(server);
            }
        }
    }

    private void frontEnd(StaticServer server) throws IOException {
        final String host = server.getHost();
        final String portSuffix = ":" + server.getPort();
        configProxyPass(server, server.getLocationUri(), host, portSuffix, false);
    }

    private void servers() throws IOException {
        final List<ApiServer> servers = entrance.getApiServers();
        if (servers != null) {
            for (ApiServer server : servers) {
                server(server);
            }
        }
    }

    private void server(ApiServer server) throws IOException {
        // 本地文件则直接获取 不然就从网上get anyway 都是一个inputStream
        final String host = server.getHost();
        final String portSuffix = ":" + server.getPort();
        try (InputStream jsonStream = apiStream(server)) {
            JsonNode paths = Builder.mapper.readTree(jsonStream).get("paths");
            if (paths == null)
                return;
            final Iterator<String> uris = paths.fieldNames();
            while (uris.hasNext()) {
                String path = uris.next();
                // 如果匹配到 {} 则替换为 .+
                // 如果匹配到了
                String locationUri;
                if (uriPattern.matcher(path).find()) {
                    String pathRegex = uriPattern.matcher(path).replaceAll(".+");
                    locationUri = "~ " + pathRegex;
                } else {
                    locationUri = "= " + path;
                }
                JsonNode uriInformation = paths.get(path);
                // 是否存在  schema 为ws or wss 的
                configProxyPass(server, locationUri, host, portSuffix, isWebSocketURI(uriInformation));
            }
        }
    }

    private boolean isWebSocketURI(JsonNode information) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(information.fields(), Spliterator.SORTED), false)
                .map(Map.Entry::getValue)
                // 只关心它的schemes
                .map(jsonNode -> jsonNode.get("schemes"))
                // 没有的就再贱了
                .filter(Objects::nonNull)
                // 目标是一个数组,全部取出来
                .flatMap(jsonNode ->
                        StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(jsonNode.elements(), Spliterator.SORTED), false
                        )
                )
                .filter(Objects::nonNull)
                .map(JsonNode::asText)
                .anyMatch(type -> "ws".equalsIgnoreCase(type) || "wss".equalsIgnoreCase(type));
    }

    private void configProxyPass(Endpoint endpoint, String locationUri, String host, String portSuffix
            , boolean isWebSocket) throws IOException {
        writeAll(String.format("\tlocation %s {\n", locationUri));
        if (endpoint.getPreBlock() != null)
            writeAll(endpoint.getPreBlock());
        if (isWebSocket)
            // https://nginx.org/en/docs/http/websocket.html
            // proxy_pass_request_headers      on;
            writeAll(String.format("\t\tproxy_pass http://%s%s;\n" +
                    "\t\tproxy_http_version 1.1;\n" +
                            "\t\tproxy_set_header Upgrade $http_Upgrade;\n" +
                            "\t\tproxy_set_header Connection \"Upgrade\";\n"
                    , host, portSuffix));
//            writeAll(String.format("\t\tproxy_pass http://%s%s;\n" +
//                    "\t\tproxy_http_version 1.1;\n" +
//                    "\t\tproxy_set_header        X-Real-IP $remote_addr;\n" +
//                    "\t\tproxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
//                    "\t\tproxy_set_header        Host $http_host;\n"+
//                    "\t\tproxy_pass_request_headers on;\n", host, portSuffix));
        else
            writeAll(String.format("\t\tproxy_pass http://%s%s;\n" +
                    "\t\tproxy_set_header        X-Real-IP $remote_addr;\n" +
                    "\t\tproxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
                    "\t\tproxy_set_header        Host $http_host;\n", host, portSuffix));

        writeHttps("\t\tproxy_set_header        X-Client-Verify  SUCCESS;\n" +
                "\t\tproxy_set_header        X-Client-DN      $ssl_client_s_dn;\n" +
                "\t\tproxy_set_header        X-SSL-Subject    $ssl_client_s_dn;\n" +
                "\t\tproxy_set_header        X-SSL-Issuer     $ssl_client_i_dn;\n");
        if (endpoint.getPostBlock() != null)
            writeAll(endpoint.getPostBlock());
        writeAll("\t}\n");
    }

    /**
     * @param server
     * @return 获取json api 的数据流
     */
    private InputStream apiStream(ApiServer server) throws FileNotFoundException {
        File target;
        if (cwd != null)
            target = new File(cwd, server.getLocalApiFile());
        else
            target = new File(server.getLocalApiFile());
        return new FileInputStream(target);
    }

    private void name() throws IOException {
        writeAll("\tserver_name  localhost;\n");
    }

    private void start() throws IOException {
        writeAll("server {\n");
    }

    private void end() throws IOException {
        // 默认的配置
//        writeAll("\n" +
//                "\tlocation / {\n" +
//                "\t\troot   /usr/share/nginx/html;\n" +
//                "\t\tindex  index.html index.htm;\n" +
//                "\t}\n" +
//                "\terror_page   500 502 503 504  /50x.html;\n" +
//                "\tlocation = /50x.html {\n" +
//                "\t\troot   /usr/share/nginx/html;\n" +
//                "\t}\n");
        writeAll("}\n");
        if (httpWriter != null)
            httpWriter.flush();
        if (httpsWriter != null)
            httpsWriter.flush();
    }

    private void port() throws IOException {
        writeHttp("\tlisten       80;\n");
        writeHttps("\tlisten       443;\n");
    }

    private void writeHttps(String str) throws IOException {
        if (httpsWriter != null)
            httpsWriter.append(str);
    }

    private void writeHttp(String str) throws IOException {
        if (httpWriter != null)
            httpWriter.append(str);
    }


    private void writeAll(String str) throws IOException {
        writeHttp(str);
        writeHttps(str);
    }


}
