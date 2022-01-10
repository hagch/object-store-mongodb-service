package object.store.daos;

import com.mongodb.reactivestreams.client.MongoCollection;
import java.util.Objects;
import java.util.UUID;
import javassist.ClassPool;
import object.store.entities.TypeDocument;
import object.store.entities.models.KeyDefinition;
import object.store.repositories.TypeRepository;
import org.bson.Document;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema.MongoJsonSchemaBuilder;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeDao(TypeRepository typeRepository, ReactiveMongoTemplate mongoTemplate) {

  public Flux<TypeDocument> getAll() {
    return typeRepository.findAll();
  }

  public Mono<TypeDocument> getById(String id) {
    return typeRepository.findById(id);
  }

  public Mono<TypeDocument> getByName(String name){
    return typeRepository.findByName(name);
  }

  public Mono<TypeDocument> createType(TypeDocument document){
    document.setId(UUID.randomUUID().toString());
    return typeRepository.save(document);
  }

  public Mono<TypeDocument> updateTypeById(TypeDocument document){
    return typeRepository.save(document);
  }
  public Mono<MongoCollection<Document>> createCollectionForType(String id) {
    return getById(id).flatMap( type -> {
      if (Objects.nonNull(type.getKeyDefinitions()) && type.getKeyDefinitions().length == 0) {
        return Mono.error(new IllegalArgumentException());
      }
      MongoJsonSchemaBuilder schema = MongoJsonSchema.builder();
      schema.additionalProperties(false);
      schema.property(JsonSchemaProperty.string("_id"));
      for (KeyDefinition keyDefinition : type.getKeyDefinitions()) {
        if(!keyDefinition.getKey().equals("id")) {
          schema.property(JsonSchemaProperty.string(keyDefinition.getKey()));
        }
      }
      CollectionOptions options = CollectionOptions
          .empty()
          .validator(Validator.schema(schema.build()))
          .strictValidation()
          .failOnValidationError();
      return mongoTemplate.createCollection(type.getName(), options);
    });
  }

}
