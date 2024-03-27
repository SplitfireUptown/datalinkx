package com.datalinkx.dataserver.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.datalinkx.dataserver.bean.domain.DsTbBean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Transactional
@Repository
public interface DsTbRepository extends CRUDRepository<DsTbBean, String> {

	Optional<DsTbBean> findByTbId(String tbId);

	DsTbBean findTopByNameAndDsId(String name, String dsId);

	List<DsTbBean> findAllByTbIdIn(List<String> tbIds);


	@Transactional
	@Modifying
	@Query(value = "update DS_TB set is_del = 1 where ds_id = :dsId", nativeQuery = true)
	void deleteByDsId(String dsId);
}
