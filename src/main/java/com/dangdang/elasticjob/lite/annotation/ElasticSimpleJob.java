package com.dangdang.elasticjob.lite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSimpleJob {
	
	// cron表达式，用于配置作业触发时间
	@AliasFor("cron")
	public abstract String value() default "";

	@AliasFor("value")
	public abstract String cron() default "";
	
	// 作业名称
	public abstract String jobName() default "";
	
	// 作业分片总数
	public abstract int shardingTotalCount() default 1;
	
	//作业分片参数
	public abstract String shardingItemParameters() default "";

	// 作业自定义参�?, 可以配置多个相同的作业，但是用不同的参数作为不同的调度实�?
	public abstract String jobParameter() default "";

	//调度监控数据�?
	public abstract String dataSource() default "";
	
	// 作业描述信息
	public abstract String description() default "";

	// 作业是否禁止启动, 可用于部署作业时，先禁止启动，部署结束后统一启动
	public abstract boolean disabled() default false;

	// 本地配置是否可覆盖注册中心配�?, 如果可覆盖，每次启动作业都以本地配置为准
	public abstract boolean overwrite() default true;
}
