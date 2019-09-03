package com.cheese.elastic.autoconfigure.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JasonLin
 * @version V1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElasticJobScheduler {
    /**
     * 作业名称
     */
    String jobName() default "";

    /**
     * 作业执行的cron表达式
     */
    String cron() default "";

    /**
     * 作业分片参数
     */
    String itemParameters() default "";

    /**
     * 作业描述
     */
    String desc() default "";

    /**
     * 作业分片总数
     */
    int shardingTotalCount() default 1;

    /**
     * 每次重启任务是否覆盖配置中心的配置信息
     */
    boolean overwrite() default false;

    /**
     * 作业错过执行时间是否立即触发一次
     */
    boolean misfire() default false;

    /**
     * 是否流式处理
     */
    boolean streamingProcess() default false;
}
