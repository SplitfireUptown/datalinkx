package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.DsBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



@Repository
public interface DsRepository extends CRUDRepository<DsBean, String> {

	Optional<DsBean> findByDsId(String dsId);

	List<DsBean> findAllByIsDel(Integer isDel);

	@Query("select d from DsBean d where d.name = :name and d.isDel = 0")
	DsBean findByName(String name);

	List<DsBean> findAllByDsIdIn(List<String> dsIds);

	@Query(value = " select * from DS where is_del = 0 "
			+ " and if(:name != '', name like concat('%', :name, '%'), 1=1) "
			+ " and if(:type != '', type = :type, 1=1) ", nativeQuery = true)
	Page<DsBean> pageQuery(Pageable pageable, String name, String type);

	@Transactional
	@Modifying
	@Query("update DsBean d set d.isDel = 1 where d.dsId = :dsId")
	void deleteByDsId(String dsId);
}
