// CHECKSTYLE:OFF
package com.datalinkx.datajob.action;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_ERROR;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import com.datalinkx.driver.model.JobContext;
import com.datalinkx.driver.utils.JobUtils;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.xxl.job.core.thread.JobThread;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public abstract class AbstractFlinkAction<T, D, U> implements IAction<T> {
    protected abstract void begin(D info);
    protected abstract void end(D info, int status, String errmsg);
    protected abstract void beforeExec(U unit) throws Exception;
    protected abstract void execute(U unit) throws Exception;
    protected abstract boolean checkResult(U unit);
    protected abstract void afterExec(U unit, boolean success, String errorMsg);
    protected abstract void cancel(U unit);
    protected abstract List<U> getExecUnitList(D info);
    protected abstract D getJobDetail(T jobParam);
    protected abstract void initUnit(U unit);

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

    @Override
    public void doAction(T actionInfo) throws Exception {
        // 获取job详情
        D detail = getJobDetail(actionInfo);
        LinkedBlockingDeque<U> runningUnit = new LinkedBlockingDeque<U>();
        LinkedBlockingDeque<U> unfinished = new LinkedBlockingDeque<>();
        Thread postThread;
        try {
            StringBuffer error = new StringBuffer();
            // 准备执行job
            begin(detail);

            // 获取job中包含的执行单元列表
            List<U> execUnits = getExecUnitList(detail);
            AtomicInteger unitCount = new AtomicInteger(execUnits.size());

            JobContext jobContext = JobUtils.cntx();
            Map<String, JobExecCountDto> countRes = FlinkAction.countRes.get();

            // 定时检查更新执行的结果
            postThread = new Thread(() -> {
                JobUtils.configContext(jobContext);
                FlinkAction.countRes.set(countRes);

                while (true) {
                    unfinished.clear();
                    while (!runningUnit.isEmpty() && !JobUtils.cntx().isCanceled()) {
                        U unit = runningUnit.poll();
                        try {
                            // 若执行成功退出，否则继续加入进行检查
                            if (checkResult(unit)) {
                                unitCount.decrementAndGet();
                                afterExec(unit, true, "success");
                            } else {
                                unfinished.add(unit);
                            }

                        } catch (Exception e) {
                            log.error("", e);
                            String errorMsg = e.getMessage();
                            error.append(errorMsg).append("\r\n");
                            unitCount.decrementAndGet();
                            log.info(errorMsg);
                            afterExec(unit, false, errorMsg);
                        }
                    }

                    // 若全部执行单元都执行完成后，推出定时检查流程
                    if (unitCount.intValue() <= 0) {
                        break;
                    }

                    if (JobUtils.cntx().isCanceled()) {
                        for (U u : runningUnit) {
                            cancel(u);
                        }

                        for (U u : unfinished) {
                            cancel(u);
                        }
                        break;
                    }

                    runningUnit.addAll(unfinished);
                    try {
                        Thread.sleep(FlinkAction.SLEEP_TIME);
                    } catch (InterruptedException e) {
                        log.error("", e);
                    }
                }

                JobUtils.configContext(null);
                FlinkAction.countRes.remove();
            }, "flink-action-post-thread");
            postThread.start();

            FlinkAction.checkThread.set(postThread);

            // 遍历执行启动flink任务
            for (U unit : execUnits) {
                try {
                    // 每个单元执行前的准备
                    if (isStop()) {
                        log.error("logkill trigger");
                        throw new InterruptedException();
                    }
                    beforeExec(unit);

                    // 启动任务
                    execute(unit);

                    // 加入正在执行的列表中
                    runningUnit.offer(unit);
                } catch (InterruptedException e) {
                    log.error("user stop", e);
                    throw e;
                } catch (Throwable e) {
                    log.error("execute flink task error.", e);
                    unitCount.decrementAndGet();
                    afterExec(unit, false, e.getMessage());
                    error.append(e.getMessage()).append("\r\n");
                }
            }

            postThread.join();

            // 整个Job结束后的处理
            end(detail, error.length() == 0 ? JOB_STATUS_SUCCESS : JOB_STATUS_ERROR, error.length() == 0 ? "success" : error.toString());
        } catch (InterruptedException e) {
            log.error("Stop task by user.");
            JobUtils.cntx().setCanceled(true);
            end(detail, JOB_STATUS_STOP, "cancel the job");
            throw e;
        } catch (Throwable e) {
            log.error("sync failed", e);
            end(detail, JOB_STATUS_ERROR, e.getMessage());
        }
    }
}
