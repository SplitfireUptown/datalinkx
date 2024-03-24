package com.datalinkx.dataserver.repository;

import java.util.List;

import com.datalinkx.dataserver.bean.domain.JobRelationBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobRelationRepository extends CRUDRepository<JobRelationBean, String> {

    @Query(value = " select * from JOB_RELATION where is_del = 0 "
            + " and if(:jobId != '', job_id = :jobId, 1=1) ", nativeQuery = true)
    Page<JobRelationBean> pageQuery(Pageable pageable, String jobId);

    List<JobRelationBean> findByIsDel(Integer isDel);

    @Modifying
    @Transactional
    @Query(value = " update JOB_RELATION set is_del = 1 where relation_id = :relationId and is_del = 0 ", nativeQuery = true)
    void logicDeleteByRelationId(String relationId);

    @Query(value = "select * from JOB_RELATION where job_id = :jobId and is_del = 0", nativeQuery = true)
    List<JobRelationBean> findSubJob(String jobId);

    @Query(value = "select * from JOB_RELATION where sub_job_id = :jobId and is_del = 0", nativeQuery = true)
    List<JobRelationBean> findParentJob(String jobId);
}
