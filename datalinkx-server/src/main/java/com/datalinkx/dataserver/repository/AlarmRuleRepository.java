package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.AlarmRuleBean;
import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AlarmRuleRepository extends CRUDRepository<AlarmRuleBean, String> {

    Optional<AlarmRuleBean> findByRuleId(String ruleId);

    List<AlarmRuleBean> findAllByNameLike(String name);

    List<AlarmRuleBean> findAllByJobId(String jobId);

    @Transactional
    @Modifying
    @Query(value = "update ALARM_RULE set is_del = 1 where job_id = :jobId", nativeQuery = true)
    void deleteByJobId(String jobId);

    @Transactional
    @Modifying
    @Query(value = "update ALARM_RULE set is_del = 1 where rule_id = :ruleId", nativeQuery = true)
    void deleteByRuleId(String ruleId);

    @Query(value =
            "SELECT ar.rule_id, ar.name AS rule_name, ar.type, ar.alarm_id, ar.status, ar.push_time, "
                    + "ac.name AS alarm_component_name, ar.job_id, j.name AS job_name "
                    + " FROM ALARM_RULE ar "
                    + " LEFT JOIN ALARM_COMPONENT ac ON ac.alarm_id = ar.alarm_id "
                    + " LEFT JOIN JOB j ON j.job_id = ar.job_id "
                    + " WHERE ar.is_del = 0 AND ac.is_del = 0 "
                    + " AND IF(COALESCE(:#{#listDto.jobIds}) IS NULL, 1=1, ar.job_id in :#{#listDto.jobIds}) "
                    + " AND IF(COALESCE(:#{#listDto.alarmIds}) IS NULL, 1=1, ac.alarm_id in :#{#listDto.alarmIds}) ", nativeQuery = true)
    List<Map<String, Object>> findAllWithCondition(@Param("listDto") AlarmRuleDto.ListDto listDto);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ALARM_RULE "
            + "SET status = CASE "
            + "                 WHEN status = 1 THEN 0 "
            + "                 WHEN status = 0 THEN 1 "
            + "                 ELSE status "
            + "              END"
            + " WHERE rule_id = :ruleId and is_del = 0", nativeQuery = true)
    void shutdown(String ruleId);
}
