
# elastic-job-lite-spring-boot-starter
spring boot starter for Elastic-Job(https://github.com/elasticjob/elastic-job)

### setup 1
***import dependency***
```java
<dependency>
    <groupId>com.github.kuhn-he</groupId>
    <artifactId>elastic-job-lite-spring-boot-starter</artifactId>
    <version>2.1.5</version>
</dependency>
```
### setup 2
***setting up application.properties***
```
#elastic-job
elaticjob.zookeeper.server-lists=127.0.0.1:2181
elaticjob.zookeeper.namespace=my-project
```
### setup 3
***define job class***
```java
import com.dangdang.elasticjob.lite.annotation.ElasticSimpleJob;
import com.dangdang.ddframe.job.api.ShardingContext;

//Job configuration annotation
@ElasticSimpleJob("0 * * * * ?")
@Component
public class MyJob implements com.dangdang.ddframe.job.api.simple.SimpleJob {

    @Override
    public void execute(ShardingContext arg0) {
        //do something
    }
}
```

### setup 4
***well done! ^_^***
