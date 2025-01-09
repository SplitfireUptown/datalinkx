package com.datalinkx.dataserver.bean.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "images", schema = "datalinkx")
public class ImageBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Column(name = "data")
    private byte[] data;

    @Size(max = 1)
    @Column(name = "is_del", length = 1)
    private String isDel;

}