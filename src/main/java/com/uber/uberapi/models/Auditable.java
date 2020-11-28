package com.uber.uberapi.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


@MappedSuperclass   //Don't create table for Auditable
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)   //jpa
    @CreatedDate    //hibernate
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate   //hibernate
    private Date updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auditable auditable = (Auditable) o;
        if(id==null || auditable.id==null)  return false;
        return id.equals(auditable.id);
    }

    @Override
    public int hashCode() {
        return id==null?0:id.hashCode();
    }
}
