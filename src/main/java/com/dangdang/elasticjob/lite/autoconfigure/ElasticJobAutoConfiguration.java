package com.dangdang.elasticjob.lite.autoconfigure;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.BooleanUtils;
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
		Map<String, SimpleJob> map = applicationContext.getBeansOfType(SimpleJob.class);
		
		for(Map.Entry<String, SimpleJob> entry : map.entrySet()){
			SimpleJob simpleJob = entry.getValue();
			ElasticSimpleJob elasticSimpleJobAnnotation=simpleJob.getClass().getAnnotation(ElasticSimpleJob.class);
			
			String cron=StringUtils.defaultIfBlank(elasticSimpleJobAnnotation.cron(), elasticSimpleJobAnnotation.value());
			String jobName=StringUtils.defaultIfBlank(elasticSimpleJobAnnotation.jobName(), simpleJob.getClass().getName());
			SimpleJobConfiguration simpleJobConfiguration=new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(jobName, cron, elasticSimpleJobAnnotation.shardingTotalCount()).shardingItemParameters(elasticSimpleJobAnnotation.shardingItemParameters()).jobParameter(elasticSimpleJobAnnotation.jobParameter()).build(), simpleJob.getClass().getCanonicalName());
			LiteJobConfiguration liteJobConfiguration=LiteJobConfiguration.newBuilder(simpleJobConfiguration).overwrite(elasticSimpleJobAnnotation.overwrite()).build();
			
			String dataSourceRef=elasticSimpleJobAnnotation.dataSource();
			if(StringUtils.isNotBlank(dataSourceRef)){
				
				if(!applicationContext.containsBean(dataSourceRef)){
					throw new RuntimeException("not exist datasource ["+dataSourceRef+"] !");
				}
				
				DataSource dataSource=(DataSource)applicationContext.getBean(dataSourceRef);
				JobEventRdbConfiguration jobEventRdbConfiguration=new JobEventRdbConfiguration(dataSource);
				SpringJobScheduler jobScheduler=new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration,jobEventRdbConfiguration);
				jobScheduler.init();
			}else{
				SpringJobScheduler jobScheduler=new SpringJobScheduler(simpleJob, regCenter, liteJobConfiguration);
				jobScheduler.init();
			}
		}
	}
}
