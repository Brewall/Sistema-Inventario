package com.edu.sistema_inventario.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedBy
    @Column(name = "audit_created_by", updatable = false)
    private String auditCreatedBy;

    @CreatedDate
    @Column(name = "audit_created_at", updatable = false)
    private LocalDateTime auditCreatedAt;

    @LastModifiedBy
    @Column(name = "audit_modified_by")
    private String auditModifiedBy;

    @LastModifiedDate
    @Column(name = "audit_modified_at")
    private LocalDateTime auditModifiedAt;

    public String getAuditCreatedBy() { return auditCreatedBy; }
    public LocalDateTime getAuditCreatedAt() { return auditCreatedAt; }
    public String getAuditModifiedBy() { return auditModifiedBy; }
    public LocalDateTime getAuditModifiedAt() { return auditModifiedAt; }
}
