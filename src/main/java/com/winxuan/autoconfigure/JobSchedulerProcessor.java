package com.winxuan.autoconfigure;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Preconditions;
import com.winxuan.annotation.ElasticJobScheduler;
import com.winxuan.properties.JobConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import java.util.Iterator;
import java.util.Map;

/**
 * @author JasonLin
 * @version V1.0
 * @date 2019/2/21
 */
@Component
public class JobSchedulerProcessor implements ApplicationContextAware, BeanFactoryAware, ApplicationListener {

    @Autowired
    private Environment environment;
    private ConfigurableListableBeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private volatile boolean started = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (started) {
            return;
        }
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ElasticJobScheduler.class);
        CoordinatorRegistryCenter regCenter = applicationContext.getBean(ZookeeperRegistryCenter.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String jobBeanName = entry.getKey();
            Object jobBean = entry.getValue();
            JobConfigProperties conf = new JobConfigProperties();
            beanConfAutowire(jobBeanName, conf);
            annotionConfAutowire(jobBean, conf);

            SpringJobScheduler jobScheduler = JobSchedulerHelper.getSpringJobScheduler((ElasticJob) jobBean, regCenter, conf);
            beanFactory.registerSingleton(defaultSchedulerName(conf, jobBeanName), jobScheduler);
            jobScheduler.init();
        }
        started = true;
    }

    private void beanConfAutowire(String prefix, JobConfigProperties conf) {
        PropertiesConfigurationFactory<Object> factory = new PropertiesConfigurationFactory<Object>(conf);
        factory.setPropertySources(deducePropertySources());
        factory.setConversionService(new DefaultConversionService());
        factory.setTargetName(prefix);
        try {
            factory.bindPropertiesToTarget();
        } catch (BindException e) {
            throw new RuntimeException(e);
        }
    }

    private void annotionConfAutowire(Object job, JobConfigProperties conf) {
        if (StringUtils.isNotBlank(conf.getCron())) {
            return;
        }

        ElasticJobScheduler anno = job.getClass().getAnnotation(ElasticJobScheduler.class);
        Preconditions.checkArgument(StringUtils.isNotBlank(anno.cron()), "must set a cron expression");
        conf.setCron(anno.cron());
        conf.setSchedulerName(anno.schedulerName());
        conf.setShardingItemParameters(anno.itemParameters());
        conf.setDescription(anno.desc());
        conf.setShardingTotalCount(anno.shardingTotalCount());
        conf.setOverwrite(anno.overwrite());
        conf.setMisfire(anno.misfire());
    }

    private String defaultSchedulerName(JobConfigProperties conf, String jobBeanName) {
        return StringUtils.isNoneBlank(conf.getSchedulerName()) ? conf.getSchedulerName() : jobBeanName + "SchedulerName";
    }

    private PropertySources deducePropertySources() {
        PropertySourcesPlaceholderConfigurer configurer = getSinglePropertySourcesPlaceholderConfigurer();
        if (configurer != null) {
            return new FlatPropertySources(configurer.getAppliedPropertySources());
        }
        if (this.environment instanceof ConfigurableEnvironment) {
            MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment)
                    .getPropertySources();
            return new FlatPropertySources(propertySources);
        }
        return new MutablePropertySources();
    }

    private PropertySourcesPlaceholderConfigurer getSinglePropertySourcesPlaceholderConfigurer() {
        if (this.beanFactory != null) {
            ConfigurableListableBeanFactory listableBeanFactory = this.beanFactory;
            Map<String, PropertySourcesPlaceholderConfigurer> beans = listableBeanFactory
                    .getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false,
                            false);
            if (beans.size() == 1) {
                return beans.values().iterator().next();
            }
        }
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                    "AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    private static class FlatPropertySources implements PropertySources {

        private PropertySources propertySources;

        FlatPropertySources(PropertySources propertySources) {
            this.propertySources = propertySources;
        }

        @Override
        public Iterator<PropertySource<?>> iterator() {
            MutablePropertySources result = getFlattened();
            return result.iterator();
        }

        @Override
        public boolean contains(String name) {
            return get(name) != null;
        }

        @Override
        public PropertySource<?> get(String name) {
            return getFlattened().get(name);
        }

        private MutablePropertySources getFlattened() {
            MutablePropertySources result = new MutablePropertySources();
            for (PropertySource<?> propertySource : this.propertySources) {
                flattenPropertySources(propertySource, result);
            }
            return result;
        }

        private void flattenPropertySources(PropertySource<?> propertySource,
                                            MutablePropertySources result) {
            Object source = propertySource.getSource();
            if (source instanceof ConfigurableEnvironment) {
                ConfigurableEnvironment environment = (ConfigurableEnvironment) source;
                for (PropertySource<?> childSource : environment.getPropertySources()) {
                    flattenPropertySources(childSource, result);
                }
            } else {
                result.addLast(propertySource);
            }
        }

    }
}
