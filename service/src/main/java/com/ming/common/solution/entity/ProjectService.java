package com.ming.common.solution.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author CJ
 */
@Entity
public class ProjectService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 服务名称
     */
    @Column(length = 50)
    private String name;
    /**
     * 这个服务所需的image
     */
    @ManyToOne
    private ImageRegister image;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageRegister getImage() {
        return image;
    }

    public void setImage(ImageRegister image) {
        this.image = image;
    }
}
