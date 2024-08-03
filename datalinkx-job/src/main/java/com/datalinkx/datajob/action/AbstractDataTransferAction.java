// CHECKSTYLE:OFF
package com.datalinkx.datajob.action;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_ERROR;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;

import java.lang.reflect.Field;
import java.util.Map;

import com.datalinkx.datajob.bean.JobExecCountDto;
import com.xxl.job.core.thread.JobThread;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public abstract class AbstractDataTransferAction<T, U> {
    protected abstract void begin(T info);
    protected abstract void end(T info, int status, String errmsg);
    protected abstract void beforeExec(U unit) throws Exception;
    protected abstract void execute(U unit) throws Exception;
    protected abstract boolean checkResult(U unit);
    protected abstract void afterExec(U unit, boolean success);
    protected abstract U convertExecUnit(T info);

    private boolean isStop() {
        JobThread jobThread = ((JobThread)Thread.currentThread());
        Field toStopField;
        boolean toStop = false;
        try {
            toStopField = jobThread.getClass().getDeclaredField("toStop");
            toStopField.setAccessible(true);
            try {
                toStop = toStopField.getBoolean(jobThread);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return toStop;
    }

    public void doAction(T actionInfo) throws Exception {
        Thread taskCheckerThread;
        try {
            StringBuffer error = new StringBuffer();
            // 1、准备执行job
            this.begin(actionInfo);

            // 2、T -> U 获取引擎执行类对象
            U execUnit = convertExecUnit(actionInfo);
            Map<String, JobExecCountDto> countRes = DataTransferAction.COUNT_RES.get();

            // 3、循环检查任务结果
            taskCheckerThread = new Thread(() -> {
                DataTransferAction.COUNT_RES.set(countRes);

                while (true) {
                    try {
                        // 3.1、如果任务执行完成
                        if (checkResult(execUnit)) {
                            // 3.2、执行任务后置处理钩子
                            this.afterExec(execUnit, true);
                            break;
                        }
                    } catch (Exception e) {
                        log.error("data-transfer-job error ", e);
                        String errorMsg = e.getMessage();
                        error.append(errorMsg).append("\r\n");
                        log.info(errorMsg);
                        this.afterExec(execUnit, false);
                    }
                }
                DataTransferAction.COUNT_RES.remove();
            }, "data-transfer-check-thread");

            // 4、执行flink任务
            try {
                // 4.1、是否用户取消任务
                if (isStop()) {
                    log.error("job shutdown trigger");
                    throw new InterruptedException();
                }

                // 4.2、每个单元执行前的准备
                this.beforeExec(execUnit);

                // 4.3、启动任务
                this.execute(execUnit);
            } catch (InterruptedException e) {
                // 用户手动取消任务
                throw e;
            } catch (Throwable e) {
                log.error("execute flink task error.", e);
                afterExec(execUnit, false);
                error.append(e.getMessage()).append("\r\n");
                // 任务提交失败，直接返回
                this.end(actionInfo, JOB_STATUS_ERROR, error.toString());
                return;
            }
            // 阻塞至任务完成
            taskCheckerThread.start();
            taskCheckerThread.join();

            // 5、整个Job结束后的处理
            this.end(actionInfo, error.length() == 0 ? JOB_STATUS_SUCCESS : JOB_STATUS_ERROR, error.length() == 0 ? "success" : error.toString());
        } catch (InterruptedException e) {
            log.error("shutdown job by user.");
            this.end(actionInfo, JOB_STATUS_STOP, "cancel the job");
            throw e;
        } catch (Throwable e) {
            log.error("transfer failed -> ", e);
            this.end(actionInfo, JOB_STATUS_ERROR, e.getMessage());
        }
    }
}
