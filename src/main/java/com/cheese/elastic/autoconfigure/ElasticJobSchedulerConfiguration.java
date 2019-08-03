package com.cheese.elastic.autoconfigure;

import com.cheese.elastic.autoconfigure.annotation.ElasticJobScheduler;
import com.cheese.elastic.autoconfigure.properties.ElasticJobProperties;
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
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JasonLin
 * @version V1.0.0
 */
public class ElasticJobSchedulerConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ElasticJobProperties properties;
    private CoordinatorRegistryCenter regCenter;

    public ElasticJobSchedulerConfiguration(ElasticJobProperties properties, CoordinatorRegistryCenter registryCenter) {
        this.properties = properties;
        this.regCenter = registryCenter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void init() {
        Map<String, SpringJobScheduler> jobSchedulers = new HashMap<>();
        //处理通过注解配置的job
        processAnnotationJob(jobSchedulers);
        //处理通过配置文件配置的job,如果已经有注解配置过,那么配置文件的会覆盖注解的
        processPropertiesJob(jobSchedulers);
        //启动jobSchedulers
        if (!jobSchedulers.isEmpty()) {
            for (SpringJobScheduler jobScheduler : jobSchedulers.values()) {
                jobScheduler.init();
            }
        }
    }

    /**
     * 通过注解配置的任务
     *
     * @param jobSchedulers
     */
    private void processAnnotationJob(Map<String, SpringJobScheduler> jobSchedulers) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ElasticJobScheduler.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String jobBeanName = entry.getKey();
            Object jobBean = entry.getValue();
            //注解处理
            ElasticJobProperties.JobProperty jobConfig = annotionConfigAutowire(jobBean);
            SpringJobScheduler jobScheduler = getSpringJobScheduler((ElasticJob) jobBean, regCenter, jobConfig);
            jobSchedulers.put(jobBeanName, jobScheduler);
        }
    }

    /**
     * 注解处理
     *
     * @param job
     * @return
     */
    private ElasticJobProperties.JobProperty annotionConfigAutowire(Object job) {
        ElasticJobProperties.JobProperty properties = new ElasticJobProperties.JobProperty();
        ElasticJobScheduler anno = job.getClass().getAnnotation(ElasticJobScheduler.class);
        checkCron(anno.cron());
        properties.setJobName(anno.jobName());
        properties.setCron(anno.cron());
        properties.setShardingItemParameters(anno.itemParameters());
        properties.setDescription(anno.desc());
        properties.setShardingTotalCount(anno.shardingTotalCount());
        properties.setOverwrite(anno.overwrite());
        properties.setMisfire(anno.misfire());
        properties.setStreamingProcess(anno.streamingProcess());
        return properties;
    }

    private void checkCron(String anno) {
        if (StringUtils.isBlank(anno)) {
            throw new IllegalArgumentException("must set a cron expression, check the conf is correct");
        }
    }

    /**
     * 配置文件配置的job
     *
     * @param jobSchedulers
     */
    private void processPropertiesJob(Map<String, SpringJobScheduler> jobSchedulers) {
        if (CollectionUtils.isEmpty(properties.getJobs())) {
            return;
        }
        for (ElasticJobProperties.JobProperty property : properties.getJobs()) {
            String jobBeanName = property.getJobName();
            ElasticJob jobBean = applicationContext.getBean(jobBeanName, ElasticJob.class);
            if (null == jobBean) {
                continue;
            }
            checkCron(property.getCron());
            SpringJobScheduler jobScheduler = getSpringJobScheduler(jobBean, regCenter, property);
            jobSchedulers.put(jobBeanName, jobScheduler);
        }
    }

    public <T extends ElasticJob> SpringJobScheduler getSpringJobScheduler(T job, CoordinatorRegistryCenter regCenter, ElasticJobProperties.JobProperty properties) {
        return new SpringJobScheduler(job, regCenter, getLiteJobConfiguration(job, properties));
    }

    private LiteJobConfiguration getLiteJobConfiguration(final ElasticJob job, ElasticJobProperties.JobProperty properties) {
        JobCoreConfiguration coreConfig = JobCoreConfiguration
                .newBuilder(StringUtils.isBlank(properties.getJobName()) ? job.getClass().getName() : properties.getJobName(), properties.getCron(), properties.getShardingTotalCount())
                .shardingItemParameters(properties.getShardingItemParameters())
                .description(properties.getDescription())
                .misfire(properties.isMisfire())
                .build();
        JobTypeConfiguration jobConfig = switchJobConfiguration(job, coreConfig, properties.isStreamingProcess());
        return LiteJobConfiguration.newBuilder(jobConfig).overwrite(properties.isOverwrite()).build();
    }

    private JobTypeConfiguration switchJobConfiguration(ElasticJob job, JobCoreConfiguration coreConfig, boolean streamingProcess) {
        if (job instanceof DataflowJob) {
            return new DataflowJobConfiguration(coreConfig, job.getClass().getCanonicalName(), streamingProcess);
        } else if (job instanceof SimpleJob) {
            return new SimpleJobConfiguration(coreConfig, job.getClass().getCanonicalName());
        } else {
            throw new RuntimeException("not support ScriptJob type");
        }
    }
}
