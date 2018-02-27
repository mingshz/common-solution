package com.ming.common.solution.entity;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.util.Map;

/**
 * @author CJ
 */
@Entity
//@Setter
//@Getter
public class RuntimeEnvironment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50)
    private String name;

    @Lob
    private String richDescription;

    @ElementCollection
    private Map<ProjectService, String> targetVersion;
    @Column(length = 50)
    private String stackName;
    /**
     * 管理主机
     */
    @ManyToOne
    private Host managerHost;
    @ManyToOne
    private Project project;


    public ProjectService watchService(ImageRegister imageRegister) {
        return targetVersion.keySet().stream()
                .filter(projectService -> projectService.getImage().equals(imageRegister))
                .findFirst().get();
    }

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

    public String getRichDescription() {
        return richDescription;
    }

    public void setRichDescription(String richDescription) {
        this.richDescription = richDescription;
    }

    public Map<ProjectService, String> getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(Map<ProjectService, String> targetVersion) {
        this.targetVersion = targetVersion;
    }

    public String getStackName() {
        return stackName;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public Host getManagerHost() {
        return managerHost;
    }

    public void setManagerHost(Host managerHost) {
        this.managerHost = managerHost;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
