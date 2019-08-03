package com.cheese.elastic.autoconfigure;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.cheese.elastic.autoconfigure.properties.ElasticJobProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author JasonLin
 * @version V1.0.0
 */
@Configuration
@ConditionalOnClass({JobScheduler.class, ElasticJob.class, SpringJobScheduler.class})
@EnableConfigurationProperties(ElasticJobProperties.class)
public class ElasticJobAutoConfiguration {

    @Bean(initMethod = "init")
    @ConditionalOnMissingBean(ZookeeperRegistryCenter.class)
    public ZookeeperRegistryCenter registryCenter(ElasticJobProperties properties) {
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(properties.getRegistry().getAddress(),
                properties.getRegistry().getNamespace()));
    }


    @Bean(initMethod = "init")
    @ConditionalOnMissingBean(ElasticJobSchedulerConfiguration.class)
    public ElasticJobSchedulerConfiguration elasticJobSchedulerConfiguration(ElasticJobProperties properties, ZookeeperRegistryCenter registryCenter) {
        return new ElasticJobSchedulerConfiguration(properties, registryCenter);
    }


}
