
package com.datalinkx.datajob.action;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_ERROR;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;

import java.lang.reflect.Field;
import java.util.Map;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.IdUtils;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.datalinkx.common.result.DatalinkXJobDetail;
import com.xxl.job.core.thread.JobThread;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public abstract class AbstractDataTransferAction<T extends DatalinkXJobDetail, U> {
    protected abstract void begin(T info);
    protected abstract void end(U unit, int status, String errmsg);
    protected abstract void beforeExec(U unit) throws Exception;
    protected abstract void execute(U unit) throws Exception;
    protected abstract boolean checkResult(U unit);
    protected abstract void afterExec(U unit, boolean success);
    protected abstract U convertExecUnit(T info) throws Exception;

    public void doAction(T actionInfo) throws Exception {
        Thread taskCheckerThread;
        // T -> U 获取引擎执行类对象
        U execUnit = convertExecUnit(actionInfo);
        try {
            StringBuffer error = new StringBuffer();
            // 1、准备执行job
            this.begin(actionInfo);

            String healthCheck = "patch-data-job-check-thread";
            if (MetaConstants.DsType.STREAM_DB_LIST.contains(actionInfo.getSyncUnit().getReader().getType())) {
                healthCheck = IdUtils.getHealthThreadName(actionInfo.getJobId());
            }

            // 3、循环检查任务结果
            taskCheckerThread = new Thread(() -> {
                while (true) {
                    try {
                        // 3.1、如果任务执行完成
                        if (checkResult(execUnit)) {
                            // 3.2、执行任务后置处理钩子
                            this.afterExec(execUnit, true);
                            break;
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.error("datalinkx job error ", e);
                        String errorMsg = e.getMessage();
                        error.append(errorMsg).append("\r\n");
                        log.info(errorMsg);
                        this.afterExec(execUnit, false);
                        break;
                    }
                }
            }, healthCheck);

            // 4、向引擎提交任务
            try {
                // 4.1、每个单元执行前的准备
                this.beforeExec(execUnit);

                // 4.2、启动任务
                this.execute(execUnit);
            } catch (Throwable e) {
                log.error("execute task error.", e);
                afterExec(execUnit, false);
                error.append(e.getMessage()).append("\r\n");
                this.end(execUnit,  JOB_STATUS_ERROR, error.toString());
                return;
            }
            // 阻塞至任务完成
            taskCheckerThread.start();
            taskCheckerThread.join();

            // 5、整个Job结束后的处理
            this.end(execUnit, error.length() == 0 ? JOB_STATUS_SUCCESS : JOB_STATUS_ERROR, error.length() == 0 ? "success" : error.toString());
        } catch (Throwable e) {
            log.error("datalinkx job failed -> ", e);
            this.end(execUnit, JOB_STATUS_ERROR, e.getMessage());
        }
    }
}
