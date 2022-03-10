package object.store.services.strategies.json.schema.property.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import object.store.services.dtos.models.KeyDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyObjectStrategy implements JsonSchemaPropertyStrategy {

  private final JsonSchemaPropertyPrimitiveStrategy primitiveStrategy;
  private final JsonSchemaPropertyArrayStrategy arrayStrategy;

  public JsonSchemaPropertyObjectStrategy(JsonSchemaPropertyPrimitiveStrategy primitiveStrategy,
      @Lazy JsonSchemaPropertyArrayStrategy arrayStrategy) {
    this.primitiveStrategy = primitiveStrategy;
    this.arrayStrategy = arrayStrategy;
  }

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(KeyDefinitionDto keyDefinitionDto) {
    String key = keyDefinitionDto.getKey();
    if (Objects.nonNull(keyDefinitionDto.getProperties()) && !keyDefinitionDto.getProperties().isEmpty()) {
      return getObjectSchema(keyDefinitionDto.getProperties(), keyDefinitionDto.getAdditionalProperties(), key);
    }
    return Mono.just(JsonSchemaProperty.object(key));
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.OBJECT;
  }

  private Mono<JsonSchemaProperty> getSchemaProperty(KeyDefinitionDto keyDefinitionDto) {
    String key = keyDefinitionDto.getKey();
    Mono<JsonSchemaProperty> property = switch (keyDefinitionDto.getType()) {
      case OBJECT -> {
        if (Objects.nonNull(keyDefinitionDto.getProperties()) && !keyDefinitionDto.getProperties().isEmpty()) {
          yield getObjectSchema(keyDefinitionDto.getProperties(), keyDefinitionDto.getAdditionalProperties(), key);
        }
        yield Mono.just(
            JsonSchemaProperty.object(key).additionalProperties(keyDefinitionDto.getAdditionalProperties()));
      }
      case ARRAY -> arrayStrategy.getArraySchema(keyDefinitionDto);
      default -> null;
    };
    if (Objects.isNull(property)) {
      return primitiveStrategy.getJsonSchemaProperty(keyDefinitionDto);
    }
    return property;
  }

  public Mono<JsonSchemaProperty[]> getSchemaProperties(List<KeyDefinitionDto> backendKeyDefinitionList) {
    List<Mono<JsonSchemaProperty>> properties = new ArrayList<>();
    for (KeyDefinitionDto keyDefinition : backendKeyDefinitionList) {
      properties.add(getSchemaProperty(keyDefinition));
    }
    return Flux.concat(properties).collectList().map(list -> list.toArray(new JsonSchemaProperty[0]));
  }

  public Mono<JsonSchemaProperty> getObjectSchema(List<KeyDefinitionDto> definitionDtos, boolean additionalProperties,
      String key) {
    return getSchemaProperties(definitionDtos).map(properties -> JsonSchemaProperty.object(key)
        .properties(properties).additionalProperties(additionalProperties));
  }
}
