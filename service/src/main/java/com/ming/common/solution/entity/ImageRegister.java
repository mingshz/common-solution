package com.ming.common.solution.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

/**
 * 镜像仓库，可以知道特定镜像从何获取
 * 目前使用的是阿里云提供的镜像仓库服务
 *
 * @author CJ
 */
@Entity
//@Setter
//@Getter
public class ImageRegister {
    //    private 供应商默认阿里云
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * cn-hangzhou
     */
    @Column(length = 50)
    private String region;
    @Column(length = 50)
    private String namespace;
    @Column(length = 50)
    private String name;
    @Column(length = 50)
    private String username;
    /**
     * 密文密码
     */
    private byte[] encodePassword;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageRegister)) return false;
        ImageRegister that = (ImageRegister) o;
        return Objects.equals(region, that.region) &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return region + "." + namespace + "/" + name;
    }

    @Override
    public int hashCode() {

        return Objects.hash(region, namespace, name);
    }

    public String toHostName(@Nullable NetworkMode mode) {
        if (region == null)
            return null;
        StringBuilder sb = new StringBuilder("registry");
        if (mode != null) {
            switch (mode) {
                case classics:
                    sb.append("-internal");
                    break;
                case vpc:
                    sb.append("-vpc");
                    break;
                default:
            }
        }
        sb.append(".").append(region).append(".aliyuncs.com");
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getEncodePassword() {
        return encodePassword;
    }

    public void setEncodePassword(byte[] encodePassword) {
        this.encodePassword = encodePassword;
    }
}
