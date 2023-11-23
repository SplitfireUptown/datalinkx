package com.datalinkx.dataserver.client.xxljob.request;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.http.Field;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XxlJobInfo {
    @Field("id")
    int id;				// 主键ID

    @Field("jobGroup")
    int jobGroup;		// 执行器主键ID

    @Field("jobDesc")
    String jobDesc;

    @Field("author")
    String author;		// 负责人

    @Field("alarmEmail")
    String alarmEmail;	// 报警邮件

    @Field("scheduleType")
    String scheduleType;			// 调度类型 NONE/CRON/FIX_RATE

    @Field("scheduleConf")
    String scheduleConf;			// 调度配置，值含义取决于调度类型

    @Field("misfireStrategy")
    String misfireStrategy;			// 调度过期策略

    @Field("executorRouteStrategy")
    String executorRouteStrategy;	// 执行器路由策略

    @Field("executorHandler")
    String executorHandler;		    // 执行器，任务Handler名称

    @Field("executorParam")
    String executorParam;		    // 执行器，任务参数

    @Field("executorBlockStrategy")
    String executorBlockStrategy;	// 阻塞处理策略

    @Field("executorTimeout")
    int executorTimeout;     		// 任务执行超时时间，单位秒

    @Field("executorFailRetryCount")
    int executorFailRetryCount;		// 失败重试次数

    @Field("glueType")
    String glueType;		// GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum

    @Field("glueSource")
    String glueSource;		// GLUE源代码

    @Field("glueRemark")
    String glueRemark;		// GLUE备注

    @Field("childJobId")
    String childJobId;	// 子任务ID，多个逗号分隔

    @Field("addTime")
    private Date addTime;

    @Field("updateTime")
    private Date updateTime;

    @Field("glueUpdatetime")
    private Date glueUpdatetime;	// GLUE更新时间

    @Field("triggerStatus")
    private int triggerStatus;		// 调度状态：0-停止，1-运行
    @Field("triggerLastTime")
    private long triggerLastTime;	// 上次调度时间
    @Field("triggerNextTime")
    private long triggerNextTime;	// 下次调度时间
}
