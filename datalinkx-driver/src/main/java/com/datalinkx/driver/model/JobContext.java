package com.datalinkx.driver.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class JobContext {
    AtomicInteger success = new AtomicInteger(0);
    AtomicInteger total = new AtomicInteger(0);

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    volatile boolean canceled = false;
    ConcurrentHashMap<String, String> nextMaxValue = new ConcurrentHashMap<>();

    public void addSuccess() {
        success.incrementAndGet();
    }

    public Integer getSuccess() {
        return success.intValue();
    }

    public void setSuccess(AtomicInteger success) {
        this.success = success;
    }

    public Integer getTotal() {
        return total.intValue();
    }

    public void setTotal(Integer total) {
        this.total.set(total);
    }


    public ConcurrentHashMap<String, String> getNextMaxValue() {
        if (nextMaxValue == null) {
            nextMaxValue = new ConcurrentHashMap<>();
        }
        return nextMaxValue;
    }

    public void setNextMaxValue(ConcurrentHashMap<String, String> nextMaxValue) {
        this.nextMaxValue = nextMaxValue;
    }
}
