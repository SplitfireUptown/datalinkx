package com.datalinkx.dataserver.repository;

import java.util.List;
import java.util.Optional;

import com.datalinkx.dataserver.bean.domain.JobBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface JobRepository extends CRUDRepository<JobBean, String> {

	Optional<JobBean> findByJobId(String jobId);

	@Query(value = "select * from JOB where is_del = 0 and job_source = 1", nativeQuery = true)
	List<JobBean> findAll();

	@Modifying
	@Transactional
	@Query(value = "update JOB set is_del = 1 where job_id = ?1", nativeQuery = true)
	void logicDeleteByJobId(String jobId);


	@Query(value = "select * from JOB where is_del = 0 ", nativeQuery = true)
	Page<JobBean> pageQuery(PageRequest pageRequest);
}
