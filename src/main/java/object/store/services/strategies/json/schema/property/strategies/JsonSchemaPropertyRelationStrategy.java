package object.store.services.strategies.json.schema.property.strategies;

import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.TypedJsonSchemaObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyRelationStrategy implements JsonSchemaPropertyStrategy {

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    return Mono.just(JsonSchemaProperty.named(keyDefinitionDto.getKey())
        .with(TypedJsonSchemaObject.of(JsonSchemaObject.Type.stringType(), JsonSchemaObject.Type.nullType())));
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.RELATION;
  }
}
