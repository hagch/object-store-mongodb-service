package object.store.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class ReactiveMongoConfiguration {

  public ReactiveMongoConfiguration() {
  }

  @Bean
  ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
    return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory);
  }

  @Bean
  TransactionalOperator transactionalOperator(ReactiveMongoTransactionManager reactiveMongoTransactionManager){
    return TransactionalOperator.create(reactiveMongoTransactionManager);
  }

}
