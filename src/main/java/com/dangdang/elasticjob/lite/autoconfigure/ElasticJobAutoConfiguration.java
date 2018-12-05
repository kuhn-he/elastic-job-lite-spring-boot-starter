package com.dangdang.elasticjob.lite.autoconfigure;

import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.elasticjob.lite.annotation.ElasticSimpleJob;

@Configuration
@ConditionalOnExpression("'${elaticjob.zookeeper.server-lists}'.length() > 0")
public class ElasticJobAutoConfiguration {

	@Value("${elaticjob.zookeeper.server-lists}")
	private String serverList;
	
	@Value("${elaticjob.zookeeper.namespace}")
	private String namespace;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@PostConstruct
	public void initElasticJob(){

		ZookeeperRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
		regCenter.init();
		Map<String, SimpleJob> simpleJobs = applicationContext.getBeansOfType(SimpleJob.class);

		simpleJobs.entrySet().stream().forEach(entry -> {
            SimpleJob simpleJob = entry.getValue();
            ElasticSimpleJob annotation = entry.getValue().getClass().getAnnotation(ElasticSimpleJob.class);

            if(Objects.isNull(annotation)) return;

            String cron = StringUtils.defaultIfBlank(annotation.cron(), annotation.value());
            String jobName = StringUtils.defaultIfBlank(annotation.jobName(), simpleJob.getClass().getName());

            JobCoreConfiguration.Builder builder = JobCoreConfiguration
                    .newBuilder(jobName, cron, annotation.shardingTotalCount());

            JobCoreConfiguration configuration = builder
                    .shardingItemParameters(annotation.shardingItemParameters())
                    .jobParameter(annotation.jobParameter())
                    .description(annotation.description())
                    .build();

            SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(
                    configuration, simpleJob.getClass().getCanonicalName());

            LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration
                    .newBuilder(simpleJobConfiguration).overwrite(annotation.overwrite()).build();

            String dataSourceRef = annotation.dataSource();

            SpringJobScheduler jobScheduler;
            if(StringUtils.isNotBlank(dataSourceRef)){

                if(!applicationContext.containsBean(dataSourceRef)){
                    throw new RuntimeException("not exist datasource ["+dataSourceRef+"] !");
                }
                DataSource dataSource = (DataSource)applicationContext.getBean(dataSourceRef);
                JobEventRdbConfiguration jobEventRdbConfiguration = new JobEventRdbConfiguration(dataSource);
                jobScheduler = new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration, jobEventRdbConfiguration);
            }else{
                jobScheduler = new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration);
            }
            jobScheduler.init();
        });
	}
}
