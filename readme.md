## spring-boot-starter-elasticjob

### 添加maven依赖
在pom文件中加入如下配置:

```
<dependency>
    <groupId>com.cheese</groupId>
    <artifactId>spring-boot-starter-elasticjob</artifactId>
    <version>1.0.0</version>
</dependency>
```
### 在配置文件中配置(application.yml)注册中心地址
- address 注册中心的地址
- namespace elastic-job的命名空间
```
elastic-job:
  registry:
    address: localhost:2181
    namespace: dip-elastic-task
```

### 使用注解配置任务
通过在任务实现类上面加入@ElasticJobScheduler注解,并配置相关作业参数.
```
@ElasticJobScheduler(cron = "0/3 * * * * ?",
        itemParameters = "0=2564",
        desc = "测试",
        shardingTotalCount = 2,
        overwrite = true,
        misfire = true)
public class SimpleAnnoJobTest implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("SimpleAnnoJobTest" + System.currentTimeMillis());
    }
}
```
### 通过配置文件配置作业
也可以通过配置文件来配置作业,如果一个job既有注解,又通过配置文件配置了相关参数,则以配置文件里面的为准.
job的名称为bean的名称,所以通过配置文件配置job则需要在job类上加上自动装配注解(@Component).

配置文件如下:
```
elastic-job:
  registry:
    address: localhost:2181
    namespace: dip-elastic-task
  jobs:
    - job-name: simplePropertyJobTest
      cron: 0/1 * * * * ?
      sharding-total-count: 1
      description: 测试任务
    - job-name: simpleAnnoJobTest
      cron: 0/1 * * * * ?
      sharding-total-count: 1
      description: 简单注解任务测试配置文件覆盖测试
``` 

任务代码如下:
```
@Component
public class SimplePropertyJobTest implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("SimplePropertyJobTest" + System.currentTimeMillis());
    }
}

@ElasticJobScheduler(cron = "0/3 * * * * ?",
        itemParameters = "0=2564",
        desc = "测试",
        shardingTotalCount = 2,
        overwrite = true,
        misfire = true)
public class SimpleAnnoJobTest implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("SimpleAnnoJobTest" + System.currentTimeMillis());
    }
}

``` 
如上所示配置了两个job,SimpleAnnoJobTest这个任务的注解配置会被配置文件里面的覆盖.

注意无论通过哪种方式配置作业,cron表达式参数必填.