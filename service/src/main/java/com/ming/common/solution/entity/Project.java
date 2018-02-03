package com.ming.common.solution.entity;

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
@Setter
@Getter
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

    @OneToMany
    private Set<ProjectService> imageSet;
    @OneToMany
    private Set<RuntimeEnvironment> environments;
}
