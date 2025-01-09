package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.ImageBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageBean, Integer> {
}
