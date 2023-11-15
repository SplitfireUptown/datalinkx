package com.dtstack.flinkx.metrics;

import com.dtstack.flinkx.constants.Metrics;
import org.apache.flink.api.common.accumulators.LongCounter;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RateCounter {
    private String name;
    private long packageCount = 0;
    private long packageLimit;
    private long packageBytes = 0;
    private long packageStartTs = 0;
    private long packageValidTs = 0;

    private LongCounter packageBytesCounter;
    private LongCounter packageCounter;
    private LongCounter packageMsCounter;
    private LongCounter packageValidTMCounter;

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public RateCounter(String name, long limit, BaseMetric metric, RuntimeContext runtimeContext) {
        this.name = name;
        this.packageLimit = limit;

        packageBytesCounter = runtimeContext.getLongCounter(name + Metrics.RATE_COUNTER_PROCESS_BYTES);
        packageCounter = runtimeContext.getLongCounter(name + Metrics.RATE_COUNTER_PROCESS_ROWS);
        packageMsCounter = runtimeContext.getLongCounter(name + Metrics.RATE_COUNTER_PROCESS_MS);
        packageValidTMCounter = runtimeContext.getLongCounter(name + Metrics.RATE_COUNTER_PROCESS_VALID_MS);

        metric.addMetric(name + Metrics.RATE_COUNTER_PROCESS_BYTES, packageBytesCounter);
        metric.addMetric(name + Metrics.RATE_COUNTER_PROCESS_ROWS, packageCounter);
        metric.addMetric(name + Metrics.RATE_COUNTER_PROCESS_MS, packageMsCounter);
        metric.addMetric(name + Metrics.RATE_COUNTER_PROCESS_VALID_MS, packageValidTMCounter);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPackageStartTs() {
        return packageStartTs;
    }

    public void setPackageStartTs(long packageStartTs) {
        this.packageStartTs = packageStartTs;
    }

    public long getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(long packageCount) {
        this.packageCount = packageCount;
    }

    public long getPackageLimit() {
        return packageLimit;
    }

    public void setPackageLimit(long packageLimit) {
        this.packageLimit = packageLimit;
    }

    public long getPackageBytes() {
        return packageBytes;
    }

    public void setPackageBytes(long packageBytes) {
        this.packageBytes = packageBytes;
    }

    public long getPackageValidTs() {
        return packageValidTs;
    }

    public void setPackageValidTs(long packageValidTs) {
        this.packageValidTs = packageValidTs;
    }

    public void reset() {
        this.packageBytes = 0;
        this.packageCount = 0;
        this.packageValidTs = 0;
        this.packageStartTs = System.currentTimeMillis();
    }

    public void addCount(long count) {
        this.packageCount += count;
    }

    public void addBytes(long bytesLen) {
        this.packageBytes += bytesLen;
    }

    public void addPackageValidTs(long t) {
        this.packageValidTs += t;
    }

    public double getValidByteRate() {
        return this.packageBytes * 1000.0 / getPackageValidTs();
    }

    public long getCostMs() {
        return System.currentTimeMillis() - this.packageStartTs;
    }

    public void record(long count, long bytesLen, long validCost) {
        addCount(count);
        addBytes(bytesLen);
        addPackageValidTs(validCost);
    }

    public void report() {
        if (getPackageCount() >= getPackageLimit()) {
            double formatValidByteRate = (double) Math.round(getValidByteRate() * 100) / 100;
            long costMs = getCostMs();
            LOG.info("{} rate counter, row: {}, bytes: {}B, cost: {}s, valid cost: {}s, valid byte rate: {} B/s",
                    getName(),
                    getPackageCount(),
                    getPackageBytes(),
                    costMs / 1000.0,
                    getPackageValidTs() / 1000.0,
                    formatValidByteRate
            );
            packageCounter.resetLocal();
            packageBytesCounter.resetLocal();
            packageMsCounter.resetLocal();
            packageValidTMCounter.resetLocal();

            packageCounter.add(getPackageCount());
            packageBytesCounter.add(getPackageBytes());
            packageMsCounter.add(costMs);
            packageValidTMCounter.add(getPackageValidTs());
            reset();
        }
    }
}
