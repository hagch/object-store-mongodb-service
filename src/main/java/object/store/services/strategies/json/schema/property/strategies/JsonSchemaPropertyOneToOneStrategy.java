package object.store.services.strategies.json.schema.property.strategies;

import object.store.services.dtos.models.KeyDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyOneToOneStrategy implements JsonSchemaPropertyStrategy {

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(KeyDefinitionDto keyDefinitionDto) {
    return null;
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.ONE_TO_ONE;
  }
}
