package com.datalinkx.dataserver.repository;

import java.util.List;
import java.util.Optional;

import com.datalinkx.dataserver.bean.domain.JobBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface JobRepository extends CRUDRepository<JobBean, String> {

	@Query(value = "select * from JOB where job_id = :jobId", nativeQuery = true)
	Optional<JobBean> findByJobId(String jobId);

	Optional<JobBean> findByName(String name);

	List<JobBean> findByJobIdIn(List<String> jobIds);

	@Query(value = "select * from JOB where type = :type and status in :jobStatus and retry_time < 5 and is_del = 0", nativeQuery = true)
	List<JobBean> findRestartJob(Integer type, List<Integer> jobStatus);

	@Query(value = "select * from JOB where type = :type and retry_time < 5 and is_del = 0", nativeQuery = true)
	List<JobBean> findRestartJob(Integer type);

	@Query(value = "select * from JOB where (reader_ds_id = :jobId or writer_ds_id = :jobId)", nativeQuery = true)
	List<JobBean> findDependJobId(String jobId);

	@Query(value = "select * from JOB where is_del = 0", nativeQuery = true)
	List<JobBean> findAll();

	@Modifying
	@Transactional
	@Query(value = "update JOB set status = :status where job_id = :jobId", nativeQuery = true)
	void updateJobStatus(String jobId, Integer status);

	@Modifying
	@Transactional
	@Query(value = "update JOB set status = :status, error_msg = :errorMsg  where job_id = :jobId", nativeQuery = true)
	void updateJobStatus(String jobId, Integer status, String errorMsg);

	@Modifying
	@Transactional
	@Query(value = "update JOB set retry_time = retry_time + 1 where job_id = :jobId", nativeQuery = true)
	void addRetryTime(String jobId);

	@Modifying
	@Transactional
	@Query(value = "update JOB set is_del = 1 where job_id = :jobId", nativeQuery = true)
	void logicDeleteByJobId(String jobId);


	@Query(value = "select * from JOB where if(:type != -1, type =:type, 1=1) AND is_del = 0", nativeQuery = true)
	Page<JobBean> pageQuery(Pageable pageable, Integer type);
}
