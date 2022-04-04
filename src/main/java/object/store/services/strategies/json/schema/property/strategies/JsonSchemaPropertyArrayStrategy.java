package object.store.services.strategies.json.schema.property.strategies;

import java.util.Objects;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
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
  private final JsonSchemaPropertyPrimitiveStrategy primitiveStrategy;

  public JsonSchemaPropertyArrayStrategy(JsonSchemaPropertyPrimitiveStrategy primitiveStrategy,
      @Lazy JsonSchemaPropertyObjectStrategy objectStrategy) {
    this.objectStrategy = objectStrategy;
    this.primitiveStrategy = primitiveStrategy;
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
      return primitiveStrategy.getJsonSchemaObject(definition.getPrimitiveArrayType())
          .map(object -> JsonSchemaProperty.array(definition.getKey()).items(JsonSchemaObject.string()));
    }
    return objectStrategy.getSchemaProperties(definition.getProperties())
        .map(properties -> JsonSchemaProperty.array(definition.getKey()).items(properties).additionalItems(
            definition.getAdditionalProperties()));
  }
}
