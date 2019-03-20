package com.winxuan.autoconfigure;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作业注册中心配置
 * 
 * @author JiaSonglin
 * @version V1.0
 * @Date 2017年5月11日
 */
@Configuration
@ConditionalOnExpression("'${regCenter.serverList}'.length() > 0")
public class RegistryCenterConfig {

	@Bean(initMethod = "init")
	public ZookeeperRegistryCenter regCenter(@Value("${regCenter.serverList}") final String serverList,
			@Value("${regCenter.namespace}") final String namespace) {
		return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
	}
}
