package com.datalinkx.dataserver.service.impl;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobRelationBean;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.repository.JobRelationRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.JobRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.datalinkx.common.utils.IdUtils.genKey;

@Service
public class JobRelationServiceImpl implements JobRelationService {

    @Autowired
    JobRelationRepository jobRelationRepository;
    @Autowired
    JobRepository jobRepository;

    public PageVo<List<JobVo.JobRelationVo>> page(JobForm.JobRelationPageForm jobRelationPageForm) {
        PageRequest pageRequest = PageRequest.of(jobRelationPageForm.getPageNo() - 1, jobRelationPageForm.getPageSize());

        AtomicReference<String> queryJobId = new AtomicReference<>("");
        if (!ObjectUtils.isEmpty(jobRelationPageForm.getJobName())) {
            jobRepository.findByName(jobRelationPageForm.getJobName()).ifPresent(jobBean -> {
                queryJobId.set(jobBean.getJobId());
            });
        }

        Page<JobRelationBean> jobRelationBeans = jobRelationRepository.pageQuery(pageRequest, queryJobId.get());

        List<String> jobIds = new ArrayList<>();
        jobRelationBeans.getContent().forEach(jobRelationBean -> {
            jobIds.add(jobRelationBean.getJobId());
            jobIds.add(jobRelationBean.getSubJobId());
        });
        Map<String, String> jobId2Name = jobRepository.findByJobIdIn(jobIds).stream().collect(Collectors.toMap(JobBean::getJobId, JobBean::getName));

        List<JobVo.JobRelationVo> jobRelationVos = jobRelationBeans.getContent().stream().map(jobRelationBean -> JobVo.JobRelationVo.builder()
                .relationId(jobRelationBean.getRelationId())
                .jobId(jobRelationBean.getJobId())
                .jobName(jobId2Name.get(jobRelationBean.getJobId()))
                .subJobId(jobRelationBean.getSubJobId())
                .subJobName(jobId2Name.get(jobRelationBean.getSubJobId()))
                .priority(jobRelationBean.getPriority())
                .build()
        ).collect(Collectors.toList());

        PageVo<List<JobVo.JobRelationVo>> result = new PageVo<>();
        result.setPageNo(jobRelationPageForm.getPageNo());
        result.setPageSize(jobRelationPageForm.getPageSize());
        result.setData(jobRelationVos);
        result.setTotalPage(jobRelationBeans.getTotalPages());
        result.setTotal(jobRelationBeans.getTotalElements());

        return result;
    }

    public void del(String relationId) {
        jobRelationRepository.logicDeleteByRelationId(relationId);
    }

    public String create(JobForm.JobRelationForm form) {
        // 1、校验是否同一job
        if (Objects.equals(form.getJobId(), form.getSubJobId())) {
           throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "任务级联配置异常，父子任务禁止相同!");
        }

        // 2、校验任务是否有循环
        this.isCyclic(form.getJobId(), form.getSubJobId());
        String relationId = genKey("relation");
        JobRelationBean jobRelationBean = new JobRelationBean();
        jobRelationBean.setJobId(form.getJobId());
        jobRelationBean.setSubJobId(form.getSubJobId());
        jobRelationBean.setPriority(form.getPriority());
        jobRelationBean.setRelationId(relationId);
        jobRelationRepository.save(jobRelationBean);
        return relationId;
    }

    // 判断是否有循环
    private void isCyclic(String jobId, String subJobId) {
        Map<String, List<String>> jobSubDepMap = jobRelationRepository.findByIsDel(0).stream().collect(
                Collectors.groupingBy(
                        JobRelationBean::getJobId,
                        Collectors.mapping(JobRelationBean::getSubJobId, Collectors.toList())
                )
        );
        List<String> job2SubList = jobSubDepMap.getOrDefault(jobId, new ArrayList<>());
        job2SubList.add(subJobId);
        jobSubDepMap.put(jobId, job2SubList);
        boolean jobCyclic = this.checkJobHasCyclicDependency(jobSubDepMap);
        if (jobCyclic) {
            throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "任务配置存在循环依赖！");
        }
    }

    // 基于拓扑算法校验任务是否存在循环依赖
    public boolean checkJobHasCyclicDependency(Map<String, List<String>> taskGraph) {
        Map<String, Integer> inDegree = new HashMap<>();

        // 初始化默认taskGraph key入度都为0
        for (String task : taskGraph.keySet()) {
            inDegree.put(task, 0);
        }

        // 根据依赖关系累加入度
        for (List<String> dependencies : taskGraph.values()) {
            for (String dependency : dependencies) {
                inDegree.put(dependency, inDegree.getOrDefault(dependency, 0) + 1);
            }
        }

        Queue<String> queue = new LinkedList<>();

        // 入度为0的根节点入队列
        for (String task : taskGraph.keySet()) {
            if (inDegree.get(task) == 0) {
                queue.offer(task);
            }
        }

        int visitedCount = 0;

        // 依次把入度为0的节点出队列，并把其子节点入度-1，为0的节点再入队列
        while (!queue.isEmpty()) {
            String task = queue.poll();
            visitedCount++;

            List<String> dependencies = taskGraph.get(task);
            if (dependencies != null) {
                for (String dependency : dependencies) {
                    inDegree.put(dependency, inDegree.getOrDefault(dependency, 0) - 1);
                    if (inDegree.get(dependency) == 0) {
                        queue.offer(dependency);
                    }
                }
            }
        }

        return visitedCount != inDegree.size();
    }

    // 任务血缘信息
    public JobVo.JobRelationBloodVo relationBloodInfo(String jobId) {
        JobVo.JobRelationBloodVo jobRelationBloodVo = new JobVo.JobRelationBloodVo();


        Set<JobVo.JobRelationBloodVoEdge> allEdges = new HashSet<>();
        this.fetchSubJobs(jobId, allEdges);
        this.fetchParentJobs(jobId, allEdges);

        jobRelationBloodVo.setEdges(new ArrayList<>(allEdges));

        List<String> jobIds = new ArrayList<>();
        allEdges.forEach(job -> {
            jobIds.add(job.getTo());
            jobIds.add(job.getFrom());
        });
        List<JobVo.JobRelationBloodVoNode> nodes = jobRepository.findByJobIdIn(jobIds)
                .stream().map(jobBean -> JobVo.JobRelationBloodVoNode.builder().id(jobBean.getJobId()).label(jobBean.getName()).build())
                        .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(nodes)) {
            jobRepository.findByJobId(jobId).ifPresent(jobBean -> {
                nodes.add(JobVo.JobRelationBloodVoNode.builder().id(jobBean.getJobId()).label(jobBean.getName()).build());
            });
        }

        jobRelationBloodVo.setNodes(nodes);
        return jobRelationBloodVo;
    }

    @Override
    public void delJobRelation(String jobId) {
        jobRelationRepository.logicDeleteByJobId(jobId);
    }

    // 递归获取上游任务节点
    private void fetchParentJobs(String jobId, Set<JobVo.JobRelationBloodVoEdge> allEdges) {
        List<JobRelationBean> parentJobs = jobRelationRepository.findParentJob(jobId);
        for (JobRelationBean subJob : parentJobs) {
            allEdges.add(JobVo.JobRelationBloodVoEdge.builder().from(subJob.getJobId()).to(subJob.getSubJobId()).build());
            fetchSubJobs(subJob.getSubJobId(), allEdges);
        }
    }

    // 递归获取下游任务节点
    private void fetchSubJobs(String jobId, Set<JobVo.JobRelationBloodVoEdge> allEdges) {
        // 下游任务集合
        List<JobRelationBean> subJobs = jobRelationRepository.findSubJob(jobId);
        for (JobRelationBean subJob : subJobs) {
            allEdges.add(JobVo.JobRelationBloodVoEdge.builder().from(subJob.getJobId()).to(subJob.getSubJobId()).build());
            fetchSubJobs(subJob.getSubJobId(), allEdges);
        }
    }
}
