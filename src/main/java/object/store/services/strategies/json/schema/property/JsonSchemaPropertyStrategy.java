package object.store.services.strategies.json.schema.property;

import object.store.dtos.models.BasicBackendDefinitionDto;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import reactor.core.publisher.Mono;

public interface JsonSchemaPropertyStrategy {

  Mono<JsonSchemaProperty> getJsonSchemaProperty(BasicBackendDefinitionDto keyDefinitionDto);

  JsonSchemaPropertyStrategyName getStrategyName();

}
