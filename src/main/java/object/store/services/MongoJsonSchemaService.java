package object.store.services;

import java.util.Arrays;
import java.util.Objects;
import object.store.constants.MongoConstants;
import object.store.dtos.TypeDto;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.gen.mongodbservice.models.BackendKeyType;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema.MongoJsonSchemaBuilder;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record MongoJsonSchemaService() {

  public Mono<CollectionOptions> generateCollectionOptions(TypeDto type) {
    return Mono.just(getSchemaProperties(type.getBackendKeyDefinitions().toArray(new BasicBackendDefinitionDto[0]))).map(
        jsonSchemaProperties -> {
          MongoJsonSchemaBuilder schema = MongoJsonSchema.builder();
          schema.additionalProperties(type.getAdditionalProperties());
          schema.properties(jsonSchemaProperties);
          return CollectionOptions.empty().validator(Validator.schema(schema.build())).strictValidation()
              .failOnValidationError();
        }
    );
  }

  private JsonSchemaProperty[] getSchemaProperties(BasicBackendDefinitionDto[] keyDefinitions) {
    return Arrays.stream(keyDefinitions).map(definition -> {
      String key = definition.getKey();
      return switch (definition.getType()) {
        case ARRAY -> {
          ArrayDefinitionDto arrayDefinition = (ArrayDefinitionDto) definition;
          if (Objects.nonNull(arrayDefinition.getPrimitiveArrayType())) {
            yield JsonSchemaProperty.array(definition.getKey())
                .items(getJsonSchemaObject(arrayDefinition.getPrimitiveArrayType()));
          }
          yield JsonSchemaProperty.array(definition.getKey()).items(JsonSchemaObject.object().properties(getSchemaProperties(arrayDefinition.getProperties().toArray(new BasicBackendDefinitionDto[0]))));
        }
        case OBJECT -> defineObject((ObjectDefinitionDto) definition);
        case ONETOMANY, ONETOONE -> JsonSchemaProperty.named(definition.getKey())
            .with(TypedJsonSchemaObject.of(Type.stringType(), Type.nullType()));
        case DATE -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key)
                .with(TypedJsonSchemaObject.of(Type.dateType(), Type.nullType()));
          }
          yield JsonSchemaProperty.date(key);
        }
        case LONG -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.longType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.longType())
              .notMatch(TypedJsonSchemaObject.of(Type.nullType())));
        }
        case DOUBLE -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.numberType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.numberType())
              .notMatch(TypedJsonSchemaObject.of(Type.nullType())));
        }
        case STRING -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key)
                .with(TypedJsonSchemaObject.of(Type.stringType(), Type.nullType()));
          }
          yield JsonSchemaProperty.string(key);
        }
        case BOOLEAN -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key)
                .with(TypedJsonSchemaObject.of(Type.booleanType(), Type.nullType()));
          }
          yield JsonSchemaProperty.bool(key);
        }
        case INTEGER -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.intType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(Type.intType())
              .notMatch(TypedJsonSchemaObject.of(Type.nullType())));
        }
        case TIMESTAMP -> {
          if (definition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(
                TypedJsonSchemaObject.of(Type.timestampType(), Type.nullType()));
          }
          yield JsonSchemaProperty.timestamp(key);
        }
        case PRIMARYKEY -> JsonSchemaProperty.string(MongoConstants.ID_NAME);
      };
    }).toList().toArray(new JsonSchemaProperty[0]);
  }

  private JsonSchemaObject getJsonSchemaObject(BackendKeyType backendKeyType) {
    return switch (backendKeyType) {
      case PRIMARYKEY -> JsonSchemaProperty.string(MongoConstants.ID_NAME);
      case DATE -> JsonSchemaObject.date();
      case TIMESTAMP -> JsonSchemaObject.timestamp();
      case DOUBLE, INTEGER, LONG -> JsonSchemaObject.number();
      case STRING -> JsonSchemaObject.string();
      case BOOLEAN -> JsonSchemaObject.bool();
      default -> null;
    };
  }

  private JsonSchemaProperty defineObject(ObjectDefinitionDto definition){
    if (Objects.nonNull(definition.getProperties()) && !definition.getProperties().isEmpty()) {
      return JsonSchemaProperty.object(definition.getKey())
          .properties(getSchemaProperties(definition.getProperties().toArray(new BasicBackendDefinitionDto[0])))
          .additionalProperties(definition.getAdditionalProperties());
    }
    return JsonSchemaProperty.object(definition.getKey()).additionalProperties(definition.getAdditionalProperties());
  }

}
