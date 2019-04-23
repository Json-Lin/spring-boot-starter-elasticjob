package com.winxuan.autoconfigure;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.winxuan.properties.JobConfigProperties;

/**
 * @author JasonLin
 * @version V1.0
 * @date 2019/2/22
 */
public class JobSchedulerHelper {

    private static LiteJobConfiguration getLiteJobConfiguration(final ElasticJob job, JobConfigProperties properties) {
        JobCoreConfiguration coreConfig = JobCoreConfiguration
                .newBuilder(job.getClass().getName(), properties.getCron(), properties.getShardingTotalCount())
                .shardingItemParameters(properties.getShardingItemParameters())
                .description(properties.getDescription())
                .misfire(properties.isMisfire())
                .build();
        JobTypeConfiguration jobConfig = switchJobConfiguration(job, coreConfig, properties.isStreamingProcess());
        return LiteJobConfiguration.newBuilder(jobConfig).overwrite(properties.isOverwrite()).build();
    }

    private static JobTypeConfiguration switchJobConfiguration(ElasticJob job, JobCoreConfiguration coreConfig, boolean streamingProcess) {
        if (job instanceof DataflowJob) {
            return new DataflowJobConfiguration(coreConfig, job.getClass().getCanonicalName(), streamingProcess);
        } else if (job instanceof SimpleJob) {
            return new SimpleJobConfiguration(coreConfig, job.getClass().getCanonicalName());
        } else {
            throw new RuntimeException("not support ScriptJob type");
        }
    }

    public static <T extends ElasticJob> SpringJobScheduler getSpringJobScheduler(T job, CoordinatorRegistryCenter regCenter, JobConfigProperties properties) {
        return new SpringJobScheduler(job, regCenter,
                getLiteJobConfiguration(job, properties));
    }
}
