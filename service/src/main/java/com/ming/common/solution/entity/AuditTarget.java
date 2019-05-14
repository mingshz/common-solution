package com.ming.common.solution.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author CJ
 */
@Entity
public class AuditTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 拒绝概率
     */
    @Column(scale = 5, precision = 10)
    private BigDecimal refuseRate = BigDecimal.ZERO;
    /**
     * 名称
     */
    @Column(length = 100)
    private String name;
    /**
     * 可信任指纹
     */
    @Column(length = 300)
    private String fingerPrint;
    /**
     * 可信任指纹类型
     */
    @Column(length = 100)
    private String fingerPrintType;
    /**
     * 主机
     */
    @Column(length = 15)
    private String host;

    @Column(columnDefinition = "datetime")
    private LocalDateTime lastVisitTime = LocalDateTime.now();

    public BigDecimal getRefuseRate() {
        return refuseRate;
    }

    public void setRefuseRate(BigDecimal refuseRate) {
        this.refuseRate = refuseRate;
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

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFingerPrintType() {
        return fingerPrintType;
    }

    public void setFingerPrintType(String fingerPrintType) {
        this.fingerPrintType = fingerPrintType;
    }

    public LocalDateTime getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(LocalDateTime lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }
}
