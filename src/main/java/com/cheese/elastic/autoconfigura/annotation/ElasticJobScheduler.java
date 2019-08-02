package com.cheese.elastic.autoconfigura.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JasonLin
 * @version V1.0
 * @date 2019/2/21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ElasticJobScheduler {

    String schedulerName() default "";

    String cron() default "";

    String itemParameters() default "";

    String desc() default "";

    int shardingTotalCount() default 1;

    boolean overwrite() default false;

    boolean misfire() default false;

    boolean streamingProcess() default false;
}
