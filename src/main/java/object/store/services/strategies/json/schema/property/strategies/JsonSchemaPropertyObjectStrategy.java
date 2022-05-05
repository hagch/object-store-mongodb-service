package object.store.services.strategies.json.schema.property.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyFactory;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyObjectStrategy implements JsonSchemaPropertyStrategy {

  private final JsonSchemaPropertyStrategyFactory strategyFactory;

  public JsonSchemaPropertyObjectStrategy(@Lazy JsonSchemaPropertyStrategyFactory strategyFactory) {
    this.strategyFactory = strategyFactory;
  }

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    ObjectDefinitionDto dto = (ObjectDefinitionDto) keyDefinitionDto;
    String key = keyDefinitionDto.getKey();
    if (Objects.nonNull(dto.getProperties()) && !dto.getProperties().isEmpty()) {
      return getObjectSchema(dto.getProperties(), dto.getAdditionalProperties(), key);
    }
    return Mono.just(JsonSchemaProperty.object(key));
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.OBJECT;
  }

  public Mono<JsonSchemaProperty[]> getSchemaProperties(List<BasicBackendDefinitionDto> backendKeyDefinitionList) {
    List<Mono<JsonSchemaProperty>> properties = new ArrayList<>();
    for (BasicBackendDefinitionDto keyDefinition : backendKeyDefinitionList) {
      if (keyDefinition instanceof ObjectDefinitionDto dto) {
        if (Objects.nonNull(dto.getProperties()) && !dto.getProperties().isEmpty()) {
          properties.add(getObjectSchema(dto.getProperties(), dto.getAdditionalProperties(), keyDefinition.getKey()));
        } else {
          properties.add(Mono.just(
              JsonSchemaProperty.object(keyDefinition.getKey()).additionalProperties(dto.getAdditionalProperties())));
        }
      } else {
        properties.add(strategyFactory.findStrategy(keyDefinition.getType()).getJsonSchemaProperty(keyDefinition));
      }
    }
    return Flux.concat(properties).collectList().map(list -> list.toArray(new JsonSchemaProperty[0]));
  }

  public Mono<JsonSchemaProperty> getObjectSchema(List<BasicBackendDefinitionDto> definitionDtos,
      boolean additionalProperties,
      String key) {
    return getSchemaProperties(definitionDtos).map(properties -> JsonSchemaProperty.object(key)
        .properties(properties).additionalProperties(additionalProperties));
  }
}
