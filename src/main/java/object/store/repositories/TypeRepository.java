package object.store.repositories;

import object.store.entities.TypeDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeRepository extends ReactiveMongoRepository<TypeDocument,String> {

}
