package com.datalinkx.dataserver.service.impl;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobLogBean;
import com.datalinkx.dataserver.bean.vo.MonitorVo;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.JobLogRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.MonitorAssetService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitorAssetServiceImpl implements MonitorAssetService {

    @Resource
    DsRepository dsRepository;

    @Resource
    JobRepository jobRepository;

    @Resource
    JobLogRepository jobLogRepository;


    @Override
    public MonitorVo.AssetTotalMonitorVo assetTotal() {
        long currentTimeStamp = System.currentTimeMillis();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long todayZeroTimeStamp = today.getTimeInMillis();

        List<DsBean> dsBeans = dsRepository.findAllByIsDel(0);
        Long dsTotalIncrease = dsBeans.stream().filter(ds -> ds.getCtime().getTime() >= todayZeroTimeStamp && ds.getCtime().getTime() <= currentTimeStamp).count();

        List<JobBean> jobBeans = jobRepository.findAll();
        long jobTotalIncrease = jobBeans.stream().filter(job -> job.getCtime().getTime() >= todayZeroTimeStamp && job.getCtime().getTime() <= currentTimeStamp).count();

        return MonitorVo.AssetTotalMonitorVo
                .builder()
                .totalDs((long) dsBeans.size())
                .todayIncreaseDs(dsTotalIncrease)
                .totalJob((long) jobBeans.size())
                .todayIncreaseJob(jobTotalIncrease)
                .build();
    }

    @Override
    public MonitorVo.AssetDsJobGroupVo assetDsJobGroupVo() {
        MonitorVo.AssetDsJobGroupVo result = new MonitorVo.AssetDsJobGroupVo();
        List<MonitorVo.ItemCountVo> itemCountVos = new ArrayList<>();

        List<JobBean> jobBeans = jobRepository.findAll();
        Map<String, Long> jobId2Count = jobLogRepository.findAllByIsDel(0).stream().collect(Collectors.groupingBy(JobLogBean::getJobId, Collectors.counting()));
        Map<String, String> jobId2Name = jobBeans.stream().collect(Collectors.toMap(JobBean::getJobId, JobBean::getName));

        jobId2Count.forEach((k, v) -> itemCountVos.add(
                MonitorVo.ItemCountVo
                       .builder()
                       .item(jobId2Name.get(k))
                       .count(v)
                       .build()
        ));
        itemCountVos.sort(Comparator.comparing(MonitorVo.ItemCountVo::getCount));
        result.setJobRunCount(itemCountVos);

        List<MonitorVo.NameTotalVo> nameTotalVos = new ArrayList<>();
        jobBeans.stream().collect(Collectors.groupingBy(JobBean::getType, Collectors.counting())).forEach((k, v) -> {
            nameTotalVos.add(
                    MonitorVo.NameTotalVo
                           .builder()
                           .name(MetaConstants.JobType.JOB_TYPE_NAME_MAP.get(k))
                           .total(v)
                           .build()
            );
        });
        nameTotalVos.sort(Comparator.comparing(MonitorVo.NameTotalVo::getTotal));
        result.setJobTypeCount(nameTotalVos);

        return result;
    }

    @Override
    public List<MonitorVo.ItemCountVo> assetJobStatus() {
        List<MonitorVo.ItemCountVo> result = new ArrayList<>();

        jobRepository.findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                JobBean::getStatus,
                                Collectors.counting()
                        )
                )
                .forEach((k, v) -> {
                    result.add(
                            MonitorVo.ItemCountVo
                                    .builder()
                                    .item(MetaConstants.JobStatus.JOB_STATUS_TO_DB_NAME_MAP.get(k))
                                    .count(v)
                                    .build()
                    );
                });
        return result;
    }
}
