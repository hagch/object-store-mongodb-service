package object.store.services.strategies.json.schema.property;

import object.store.services.dtos.models.KeyDefinitionDto;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import reactor.core.publisher.Mono;

public interface JsonSchemaPropertyStrategy {

  Mono<JsonSchemaProperty> getJsonSchemaProperty(KeyDefinitionDto keyDefinitionDto);

  JsonSchemaPropertyStrategyName getStrategyName();

}
