package com.ming.common.solution.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import me.jiangcai.crud.CrudFriendly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * @author CJ
 */
@Entity
public class Project implements CrudFriendly<String> {
    @Id
    @Column(length = 30)
    private String id;
    @Lob
    private String description;
    /**
     * 默认的分支
     */
    @Column(length = 30)
    private String branch;
    /**
     * 图像url
     */
    @Column(length = 100)
    private String avatar;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private Set<ProjectService> imageSet;
    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private Set<RuntimeEnvironment> environments;
    /**
     * 项目相关人
     */
    @JsonIgnore
    @OneToMany
    private Set<User> relates;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Set<ProjectService> getImageSet() {
        return imageSet;
    }

    public void setImageSet(Set<ProjectService> imageSet) {
        this.imageSet = imageSet;
    }

    public Set<RuntimeEnvironment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<RuntimeEnvironment> environments) {
        this.environments = environments;
    }

    public Set<User> getRelates() {
        return relates;
    }

    public void setRelates(Set<User> relates) {
        this.relates = relates;
    }
}
