package com.winxuan.properties;


/**
 * 将作业的配置信息封装为对象
 *
 * @author JasonLin
 * @version V1.0
 * @date 2019/2/21
 */
public class JobConfigProperties {

    /**
     * 作业执行的cron表达式
     */
    private String cron;
    /**
     * 作业调度器名称
     */
    private String schedulerName;
    /**
     * 作业分片参数
     */
    private String shardingItemParameters;
    /**
     * 作业描述
     */
    private String description;
    /**
     * 作业分片总数
     */
    private int shardingTotalCount;
    /**
     * 每次重启任务是否覆盖配置中心的配置信息
     */
    private boolean overwrite;
    /**
     * 作业错过执行时间是否立即触发一次
     */
    private boolean misfire;

    private boolean streamingProcess;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getShardingTotalCount() {
        return shardingTotalCount;
    }

    public void setShardingTotalCount(int shardingTotalCount) {
        this.shardingTotalCount = shardingTotalCount;
    }

    public String getShardingItemParameters() {
        return shardingItemParameters;
    }

    public void setShardingItemParameters(String shardingItemParameters) {
        this.shardingItemParameters = shardingItemParameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isMisfire() {
        return misfire;
    }

    public void setMisfire(boolean misfire) {
        this.misfire = misfire;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public boolean isStreamingProcess() {
        return streamingProcess;
    }

    public void setStreamingProcess(boolean streamingProcess) {
        this.streamingProcess = streamingProcess;
    }
}
