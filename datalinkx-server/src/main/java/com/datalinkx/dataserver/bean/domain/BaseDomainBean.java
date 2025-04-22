package com.datalinkx.dataserver.bean.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder(toBuilder = true)
public class BaseDomainBean {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "ctime")
    public Timestamp ctime;
    @Column(name = "utime")
    public Timestamp utime;
    @Column(name = "is_del")
    public Integer isDel = 0;
}
