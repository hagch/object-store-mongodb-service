package object.store.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import object.store.constants.MongoConstants;
import object.store.daos.entities.TypeDocument;
import object.store.daos.entities.models.KeyDefinitionModel;
import object.store.services.dtos.TypeDto;
import object.store.services.dtos.models.KeyDefinitionDto;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema.MongoJsonSchemaBuilder;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Service;

@Service
public class MongoJsonSchemaService {

  public CollectionOptions generateCollectionOptions(TypeDto type) {
    MongoJsonSchemaBuilder schema = MongoJsonSchema.builder();
    schema.additionalProperties(type.isHasAdditionalProperties());
    JsonSchemaProperty[] properties;
    properties = getSchemaProperties(type.getBackendKeyDefinitions(), type.isHasAdditionalProperties());
    schema.properties(properties);
    return CollectionOptions.empty().validator(Validator.schema(schema.build())).strictValidation()
        .failOnValidationError();
  }

  private JsonSchemaProperty[] getSchemaProperties(List<KeyDefinitionDto> backendKeyDefinitionList,
      boolean additionalProperties) {
    List<JsonSchemaProperty> properties = new ArrayList<>();
    for (KeyDefinitionDto keyDefinition : backendKeyDefinitionList) {
      String key = keyDefinition.getKey();
      JsonSchemaProperty property = switch (keyDefinition.getType()) {
        case PRIMARYKEY -> JsonSchemaProperty.string(MongoConstants.ID_NAME);
        case DATE -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key)
                .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.dateType(), JsonSchemaObject.Type.nullType()));
          }
          yield JsonSchemaProperty.date(key);
        }
        case TIMESTAMP -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(
                TypedJsonSchemaObject.of(JsonSchemaObject.Type.timestampType(), JsonSchemaObject.Type.nullType()));
          }
          yield JsonSchemaProperty.timestamp(key);
        }
        case DOUBLE -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.numberType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.numberType())
              .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
        }
        case INTEGER -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.intType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.intType())
              .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
        }
        case LONG -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.longType()));
          }
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.longType())
              .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
        }
        case STRING -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key)
                .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.stringType(), JsonSchemaObject.Type.nullType()));
          }
          yield JsonSchemaProperty.string(key);
        }
        case BOOLEAN -> {
          if (keyDefinition.getIsNullAble()) {
            yield JsonSchemaProperty.named(key)
                .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.booleanType(), Type.nullType()));
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

  private JsonSchemaProperty getArraySchema(KeyDefinitionDto definition, boolean additionalProperties) {
    if (Objects.nonNull(definition.getPrimitiveArrayType())) {
      return JsonSchemaProperty.array(definition.getKey()).items(JsonSchemaObject.string());
    }
    return JsonSchemaProperty.array(definition.getKey()).items(
        JsonSchemaObject.object().properties(getSchemaProperties(definition.getProperties(), additionalProperties)));
  }

}
