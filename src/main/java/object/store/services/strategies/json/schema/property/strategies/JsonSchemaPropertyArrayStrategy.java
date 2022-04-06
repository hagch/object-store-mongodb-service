package object.store.services.strategies.json.schema.property.strategies;

import java.util.Objects;
import object.store.constants.MongoConstants;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyArrayStrategy implements JsonSchemaPropertyStrategy {

  private final JsonSchemaPropertyObjectStrategy objectStrategy;

  public JsonSchemaPropertyArrayStrategy(@Lazy JsonSchemaPropertyObjectStrategy objectStrategy) {
    this.objectStrategy = objectStrategy;
  }

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    return getArraySchema((ArrayDefinitionDto) keyDefinitionDto);
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.ARRAY;
  }

  public Mono<JsonSchemaProperty> getArraySchema(ArrayDefinitionDto definition) {
    if (Objects.nonNull(definition.getPrimitiveArrayType())) {
      return getJsonSchemaObject(definition.getPrimitiveArrayType())
          .map(object -> JsonSchemaProperty.array(definition.getKey()).items(JsonSchemaObject.string()));
    }
    return objectStrategy.getSchemaProperties(definition.getProperties())
        .map(properties -> JsonSchemaProperty.array(definition.getKey()).items(properties).additionalItems(
            definition.getAdditionalProperties()));
  }

  private Mono<JsonSchemaObject> getJsonSchemaObject(BackendKeyType backendKeyType) {
    return Mono.justOrEmpty(switch (backendKeyType) {
      case PRIMARYKEY -> JsonSchemaProperty.string(MongoConstants.ID_NAME);
      case DATE -> JsonSchemaObject.date();
      case TIMESTAMP -> JsonSchemaObject.timestamp();
      case DOUBLE, INTEGER, LONG -> JsonSchemaObject.number();
      case STRING -> JsonSchemaObject.string();
      case BOOLEAN -> JsonSchemaObject.bool();
      default -> null;
    });
  }
}
