package com.datalinkx.dataserver.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.datalinkx.dataserver.bean.domain.DsTbBean;
import org.springframework.stereotype.Repository;


@Transactional
@Repository
public interface DsTbRepository extends CRUDRepository<DsTbBean, String> {

	Optional<DsTbBean> findByTbId(String tbId);

	DsTbBean findTopByNameAndDsId(String name, String dsId);

	List<DsTbBean> findAllByTbIdIn(List<String> tbIds);
}
