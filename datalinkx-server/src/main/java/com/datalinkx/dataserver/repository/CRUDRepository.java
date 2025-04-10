package com.datalinkx.dataserver.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface CRUDRepository<T, ID> extends Repository<T, ID> {
    <S extends T> S save(S var1);

    Iterable<T> findAll();
}