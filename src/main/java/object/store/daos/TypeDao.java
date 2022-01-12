package object.store.daos;

import com.mongodb.reactivestreams.client.MongoCollection;
import java.util.Objects;
import java.util.UUID;
import javassist.ClassPool;
import object.store.constants.MongoConstants;
import object.store.entities.TypeDocument;
import object.store.entities.models.KeyDefinition;
import object.store.gen.mongodbservice.models.BackendKeyType;
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
  public Mono<TypeDocument> createCollectionForType(TypeDocument document) {
    return Mono.just(document).flatMap( type -> {
      if (Objects.nonNull(type.getBackendKeyDefinitions()) && type.getBackendKeyDefinitions().length == 0) {
        return Mono.error(new IllegalArgumentException());
      }
      MongoJsonSchemaBuilder schema = MongoJsonSchema.builder();
      schema.additionalProperties(type.isHasAdditionalProperties());
      schema.property(JsonSchemaProperty.string(MongoConstants.ID_NAME));
      for (KeyDefinition keyDefinition : type.getBackendKeyDefinitions()) {
        if(!BackendKeyType.PRIMARYKEY.equals(keyDefinition.getType())) {
          String key = keyDefinition.getKey();
          JsonSchemaProperty property = switch(keyDefinition.getType()){
            case DATE -> JsonSchemaProperty.date(key);
            case TIMESTAMP -> JsonSchemaProperty.timestamp(key);
            case DOUBLE -> JsonSchemaProperty.decimal128(key);
            case INTEGER -> JsonSchemaProperty.int32(key);
            case LONG -> JsonSchemaProperty.int64(key);
            case STRING -> JsonSchemaProperty.string(key);
            case BOOLEAN -> JsonSchemaProperty.bool(key);
            case OBJECT -> JsonSchemaProperty.object(key);
            case ARRAY -> JsonSchemaProperty.array(key);
            default -> null;
          };
          if(property == null){
            return Mono.error(new IllegalArgumentException());
          }
          schema.property(property);
        }
      }
      CollectionOptions options = CollectionOptions
          .empty()
          .validator(Validator.schema(schema.build()))
          .strictValidation()
          .failOnValidationError();
      return mongoTemplate.createCollection(type.getName(), options).thenReturn(type);
    });
  }

}
