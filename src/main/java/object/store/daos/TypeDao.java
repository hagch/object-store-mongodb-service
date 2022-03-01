package object.store.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import object.store.constants.MongoConstants;
import object.store.entities.TypeDocument;
import object.store.entities.models.KeyDefinition;
import object.store.repositories.TypeRepository;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema.MongoJsonSchemaBuilder;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject;
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
      MongoJsonSchemaBuilder schema = MongoJsonSchema.builder();
      schema.additionalProperties(type.isHasAdditionalProperties());
      JsonSchemaProperty[] properties;
      try {
        properties = getSchemaProperties(type.getBackendKeyDefinitions(),
            type.isHasAdditionalProperties());
      } catch (IllegalArgumentException e) {
        return Mono.error(e);
      }
      schema.properties(properties);
      CollectionOptions options = CollectionOptions
          .empty()
          .validator(Validator.schema(schema.build()))
          .strictValidation()
          .failOnValidationError();
      return mongoTemplate.createCollection(type.getName(), options).thenReturn(type);
    });
  }

  private JsonSchemaProperty[] getSchemaProperties(List<KeyDefinition> backendKeyDefinitionList,
      boolean additionalProperties) {
    List<JsonSchemaProperty> properties = new ArrayList<>();
    for (KeyDefinition keyDefinition : backendKeyDefinitionList) {
      String key = keyDefinition.getKey();
      JsonSchemaProperty property = switch (keyDefinition.getType()) {
        case PRIMARYKEY -> JsonSchemaProperty.string(MongoConstants.ID_NAME);
        case DATE -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.dateType(), Type.nullType()));
          }
          yield JsonSchemaProperty.date(key);
        }
        case TIMESTAMP -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.timestampType(), Type.nullType()));
          }
          yield JsonSchemaProperty.timestamp(key);
        }
        case DOUBLE -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.numberType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.numberType())
              .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
        }
        case INTEGER -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.intType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.intType())
              .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
        }
        case LONG -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.longType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.longType())
              .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
        }
        case STRING -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.stringType(), Type.nullType()));
          }
          yield JsonSchemaProperty.string(key);
        }
        case BOOLEAN -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.booleanType(), Type.nullType()));
          }
          yield JsonSchemaProperty.bool(key);
        }
        case OBJECT -> {
          if (Objects.nonNull(keyDefinition.getProperties()) && !keyDefinition.getProperties().isEmpty()) {
            yield JsonSchemaProperty.object(key)
                .properties(getSchemaProperties(keyDefinition.getProperties(), additionalProperties));
          }
          yield JsonSchemaProperty.object(key);
        }
        case ARRAY -> getArraySchema(keyDefinition, additionalProperties);
      };
      properties.add(property);
    }
    JsonSchemaProperty[] itemsArray = new JsonSchemaProperty[properties.size()];
    return properties.toArray(itemsArray);
  }

  private JsonSchemaProperty getArraySchema(KeyDefinition definition,
      boolean additionalProperties) {
    if (Objects.nonNull(definition.getPrimitiveArrayType())) {
      return JsonSchemaProperty.array(definition.getKey()).items(JsonSchemaObject.string());
    }
    return JsonSchemaProperty.array(definition.getKey()).items(JsonSchemaObject.object().properties(
        getSchemaProperties(definition.getProperties(),
            additionalProperties)));
  }
}