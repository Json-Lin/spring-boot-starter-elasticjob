package com.cheese.elastic.autoconfigura.autoconfigure;

import com.cheese.elastic.autoconfigura.annotation.ElasticJobScheduler;
import com.cheese.elastic.autoconfigura.properties.ElasticJobProperties;
import com.dangdang.ddframe.job.api.ElasticJob;
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
 * @version V1.0
 * @date 2019/8/2
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
            //根据配置文件配置作业
            ElasticJobProperties.JobProperty jobConfig = properties.getJobConfiguration(jobBeanName);
            //注解处理
            annotionConfigAutowire(jobBean, jobConfig);
            SpringJobScheduler jobScheduler = JobSchedulerHelper.getSpringJobScheduler((ElasticJob) jobBean, regCenter, jobConfig);
            jobSchedulers.put(jobBeanName, jobScheduler);
        }
    }

    /**
     * 注解处理
     *
     * @param job
     * @param properties
     */
    private void annotionConfigAutowire(Object job, ElasticJobProperties.JobProperty properties) {
        if (null == properties || StringUtils.isNotBlank(properties.getCron())) {
            return;
        }

        ElasticJobScheduler anno = job.getClass().getAnnotation(ElasticJobScheduler.class);
        Preconditions.checkArgument(StringUtils.isNotBlank(anno.cron()), job.getClass().getName() + "must set a cron expression, check the conf is correct");
        properties.setCron(anno.cron());
        properties.setSchedulerName(anno.schedulerName());
        properties.setShardingItemParameters(anno.itemParameters());
        properties.setDescription(anno.desc());
        properties.setShardingTotalCount(anno.shardingTotalCount());
        properties.setOverwrite(anno.overwrite());
        properties.setMisfire(anno.misfire());
        properties.setStreamingProcess(anno.streamingProcess());
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
            SpringJobScheduler jobScheduler = JobSchedulerHelper.getSpringJobScheduler(jobBean, regCenter, property);
            jobSchedulers.put(jobBeanName, jobScheduler);
        }
    }
}
