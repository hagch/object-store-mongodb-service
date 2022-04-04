package object.store.services;

import java.util.ArrayList;
import java.util.List;
import object.store.dtos.TypeDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.services.strategies.json.schema.property.JsonSchemaPropertyStrategyFactory;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema.MongoJsonSchemaBuilder;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record MongoJsonSchemaService(JsonSchemaPropertyStrategyFactory strategyFactory) {

  public Mono<CollectionOptions> generateCollectionOptions(TypeDto type) {
    return getSchemaProperties(type.getBackendKeyDefinitions()).map((properties) -> {
      MongoJsonSchemaBuilder schema = MongoJsonSchema.builder();
      schema.additionalProperties(type.getAdditionalProperties());
      schema.properties(properties);
      return CollectionOptions.empty().validator(Validator.schema(schema.build())).strictValidation()
          .failOnValidationError();
    });
  }

  private Mono<JsonSchemaProperty[]> getSchemaProperties(List<BasicBackendDefinitionDto> backendKeyDefinitionList) {
    List<Mono<JsonSchemaProperty>> properties = new ArrayList<>();
    for (BasicBackendDefinitionDto keyDefinition : backendKeyDefinitionList) {
      properties.add(strategyFactory.findStrategy(keyDefinition).getJsonSchemaProperty(keyDefinition));
    }
    return Flux.concat(properties).collectList().map(list -> new JsonSchemaProperty[0]);
  }

}
