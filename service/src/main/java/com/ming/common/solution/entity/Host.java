package com.ming.common.solution.entity;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSchException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Base64;
import java.util.Objects;

/**
 * 表示一台真正的主机
 *
 * @author CJ
 */
@Entity
public class Host {
    @Id
    @Column(length = 100, name = "id")
    private String host;
    private int port = 22;

    /**
     * 是否采用严格服务器检查
     */
    private boolean strictHostKeyChecking = true;
    /**
     * 服务器指纹
     */
    @Lob
    @Column(name = "serverKey")
    private String key;
    private NetworkMode mode;
    /**
     * 管理用户
     */
    @Column(length = 50)
    private String managerUser;

    private byte[] managerPrivateKeyData;
    @Column(length = 50)
    private String managerPassPhrase;

    public HostKey toKey() throws JSchException {
        return new com.jcraft.jsch.HostKey(host, com.jcraft.jsch.HostKey.SSHRSA, Base64.getDecoder().decode(key));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Host)) return false;
        Host host1 = (Host) o;
        return Objects.equals(host, host1.host);
    }

    @Override
    public int hashCode() {

        return Objects.hash(host);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public NetworkMode getMode() {
        return mode;
    }

    public void setMode(NetworkMode mode) {
        this.mode = mode;
    }

    public String getManagerUser() {
        return managerUser;
    }

    public void setManagerUser(String managerUser) {
        this.managerUser = managerUser;
    }

    public byte[] getManagerPrivateKeyData() {
        return managerPrivateKeyData;
    }

    public void setManagerPrivateKeyData(byte[] managerPrivateKeyData) {
        this.managerPrivateKeyData = managerPrivateKeyData;
    }

    public String getManagerPassPhrase() {
        return managerPassPhrase;
    }

    public void setManagerPassPhrase(String managerPassPhrase) {
        this.managerPassPhrase = managerPassPhrase;
    }
}
