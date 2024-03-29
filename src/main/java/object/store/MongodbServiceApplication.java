package object.store;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableReactiveMongoRepositories
public class MongodbServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(MongodbServiceApplication.class)
        .web(WebApplicationType.REACTIVE)
        .run(args);
  }
}
