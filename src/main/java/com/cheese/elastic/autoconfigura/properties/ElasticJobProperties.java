package com.cheese.elastic.autoconfigura.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JasonLin
 * @version V1.0
 * @date 2019/8/2
 */
@ConfigurationProperties("elastic-job")
public class ElasticJobProperties {

    /**
     * 注册中心配置
     */
    private Registry registry;
    /**
     * 任务配置
     */
    private List<JobProperty> jobs;

    private Map<String, JobProperty> jobPropertiesMap;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public List<JobProperty> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobProperty> jobs) {
        initJobPropertiesMap(jobs);
        this.jobs = jobs;
    }

    private void initJobPropertiesMap(List<JobProperty> jobs) {
        if (null == jobPropertiesMap && !CollectionUtils.isEmpty(jobs)) {
            jobPropertiesMap = new HashMap<>();
            for (JobProperty job : jobs) {
                jobPropertiesMap.put(job.getJobName(), job);
            }
        }
    }

    public JobProperty getJobConfiguration(String jobName) {
        initJobPropertiesMap(jobs);
        return jobPropertiesMap.get(jobName);
    }

    public static class Registry {
        /**
         * 注册中心地址
         */
        private String address;
        /**
         * 命名空间
         */
        private String namespace;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }

    public static class JobProperty {
        private String jobName;
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

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }
    }
}
