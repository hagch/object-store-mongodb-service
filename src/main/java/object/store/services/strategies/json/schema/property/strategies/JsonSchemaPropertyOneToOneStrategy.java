package object.store.services.strategies.json.schema.property.strategies;

import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategy;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyName;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonSchemaPropertyOneToOneStrategy implements JsonSchemaPropertyStrategy {

  @Override
  public Mono<JsonSchemaProperty> getJsonSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto) {
    return null;
  }

  @Override
  public JsonSchemaPropertyStrategyName getStrategyName() {
    return JsonSchemaPropertyStrategyName.ONE_TO_ONE;
  }
}
