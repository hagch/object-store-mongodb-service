package object.store.services.strategies.json.schema.property.strategies;

import object.store.constants.MongoConstants;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyPrimitiveStrategy implements JsonSchemaPropertyStrategy {

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    return Mono.justOrEmpty(getSchemaProperty(keyDefinitionDto));
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.PRIMITIVE;
  }

  private JsonSchemaProperty getSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    String key = keyDefinitionDto.getKey();
    return switch (keyDefinitionDto.getType()) {
      case PRIMARYKEY -> JsonSchemaProperty.string(MongoConstants.ID_NAME);
      case DATE -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key)
              .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.dateType(), JsonSchemaObject.Type.nullType()));
        }
        yield JsonSchemaProperty.date(key);
      }
      case TIMESTAMP -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key).with(
              TypedJsonSchemaObject.of(JsonSchemaObject.Type.timestampType(), JsonSchemaObject.Type.nullType()));
        }
        yield JsonSchemaProperty.timestamp(key);
      }
      case DOUBLE -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.numberType()));
        }
        yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.numberType())
            .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
      }
      case INTEGER -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.intType()));
        }
        yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.intType())
            .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
      }
      case LONG -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.longType()));
        }
        yield JsonSchemaProperty.named(key).with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.longType())
            .notMatch(TypedJsonSchemaObject.of(JsonSchemaObject.Type.nullType())));
      }
      case STRING -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key)
              .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.stringType(), JsonSchemaObject.Type.nullType()));
        }
        yield JsonSchemaProperty.string(key);
      }
      case BOOLEAN -> {
        if (keyDefinitionDto.getIsNullAble()) {
          yield JsonSchemaProperty.named(key)
              .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.booleanType(), Type.nullType()));
        }
        yield JsonSchemaProperty.bool(key);
      }
      default -> null;
    };
  }
}
