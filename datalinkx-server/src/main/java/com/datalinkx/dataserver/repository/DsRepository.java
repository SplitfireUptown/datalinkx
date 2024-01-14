package com.datalinkx.dataserver.repository;

import java.util.List;
import java.util.Optional;

import com.datalinkx.dataserver.bean.domain.DsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



@Repository
public interface DsRepository extends CRUDRepository<DsBean, String> {

	Optional<DsBean> findByDsId(String dsId);

	List<DsBean> findAllByIsDel(Integer isDel);

	@Query(value = "select * from DS where name = :name and is_del = 0", nativeQuery = true)
	DsBean findByName(String name);

	List<DsBean> findAllByDsIdIn(List<String> dsIds);

	@Query(value = " select * from DS where is_del = 0 "
			+ " and if(:name != '', name like concat('%', :name, '%'), 1=1) "
			+ " and if(:type != '', type = :type, 1=1) ", nativeQuery = true)
    Page<DsBean> pageQuery(Pageable pageable, String name, Integer type);

	@Transactional
	@Modifying
	@Query(value = "update DS set is_del = 1 where ds_id = :dsId", nativeQuery = true)
	void deleteByDsId(String dsId);
}
