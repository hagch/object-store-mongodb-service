package object.store.daos;

import java.util.Objects;
import java.util.UUID;
import object.store.entities.TypeDocument;
import object.store.repositories.TypeRepository;
import object.store.services.MongoJsonSchemaService;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeDao(TypeRepository typeRepository, MongoJsonSchemaService mongoJsonSchemaService,
                      ReactiveMongoTemplate mongoTemplate) {

  public Flux<TypeDocument> getAll() {
    return typeRepository.findAll();
  }

  public Mono<TypeDocument> getById(String id) {
    return typeRepository.findById(id);
  }

  public Mono<TypeDocument> getByName(String name) {
    return typeRepository.findByName(name);
  }

  public Mono<TypeDocument> createType(TypeDocument document) {
    document.setId(UUID.randomUUID().toString());
    return typeRepository.save(document);
  }

  public Mono<TypeDocument> updateTypeById(TypeDocument document) {
    return typeRepository.save(document);
  }

  public Mono<TypeDocument> createCollectionForType(TypeDocument document) {
    return Mono.just(document).flatMap(type -> {
      if (Objects.isNull(type.getBackendKeyDefinitions()) || type.getBackendKeyDefinitions().size() == 0) {
        return Mono.error(new IllegalArgumentException());
      }
      CollectionOptions options = mongoJsonSchemaService.generateCollectionOptions(type);
      return mongoTemplate.createCollection(type.getName(), options).thenReturn(type);
    });
  }

  public Mono<Void> delete(String id) {
    return typeRepository.findById(id).flatMap(document -> Mono.zip(typeRepository.delete(document),
        mongoTemplate.dropCollection(document.getName())).then());
  }
}