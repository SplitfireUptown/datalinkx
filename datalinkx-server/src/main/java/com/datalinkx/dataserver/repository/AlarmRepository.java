package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.AlarmComponentBean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmRepository extends CRUDRepository<AlarmComponentBean, String> {

    Optional<AlarmComponentBean> findByAlarmId(String alarmId);

    List<AlarmComponentBean> findAllByAlarmIdIn(List<String> alarmIds);

    List<AlarmComponentBean> findAllByNameLike(String name);

    @Query(value = "select * from ALARM_COMPONENT where is_del = 0", nativeQuery = true)
    List<AlarmComponentBean> findAll();

    @Transactional
    @Modifying
    @Query(value = "update ALARM_COMPONENT set is_del = 1 where alarm_id = :alarmId", nativeQuery = true)
    void delete(String alarmId);
}
