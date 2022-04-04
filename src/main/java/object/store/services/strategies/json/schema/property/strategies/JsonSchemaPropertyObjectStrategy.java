package object.store.services.strategies.json.schema.property.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
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

  private Mono<JsonSchemaProperty> getSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    String key = keyDefinitionDto.getKey();
    if(keyDefinitionDto instanceof ObjectDefinitionDto dto){
      if (Objects.nonNull(dto.getProperties()) && !dto.getProperties().isEmpty()) {
        return getObjectSchema(dto.getProperties(), dto.getAdditionalProperties(), key);
      }
      return Mono.just(
          JsonSchemaProperty.object(key).additionalProperties(dto.getAdditionalProperties()));
    }
    if( keyDefinitionDto instanceof ArrayDefinitionDto dto){
      return arrayStrategy.getArraySchema(dto);
    }
    return primitiveStrategy.getJsonSchemaProperty(keyDefinitionDto);
  }

  public Mono<JsonSchemaProperty[]> getSchemaProperties(List<BasicBackendDefinitionDto> backendKeyDefinitionList) {
    List<Mono<JsonSchemaProperty>> properties = new ArrayList<>();
    for (BasicBackendDefinitionDto keyDefinition : backendKeyDefinitionList) {
      properties.add(getSchemaProperty(keyDefinition));
    }
    return Flux.concat(properties).collectList().map(list -> list.toArray(new JsonSchemaProperty[0]));
  }

  public Mono<JsonSchemaProperty> getObjectSchema(List<BasicBackendDefinitionDto> definitionDtos, boolean additionalProperties,
      String key) {
    return getSchemaProperties(definitionDtos).map(properties -> JsonSchemaProperty.object(key)
        .properties(properties).additionalProperties(additionalProperties));
  }
}
