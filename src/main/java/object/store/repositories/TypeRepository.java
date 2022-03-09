package object.store.repositories;

import object.store.daos.entities.TypeDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TypeRepository extends ReactiveMongoRepository<TypeDocument, String> {

  Mono<TypeDocument> findByName(String name);
}
