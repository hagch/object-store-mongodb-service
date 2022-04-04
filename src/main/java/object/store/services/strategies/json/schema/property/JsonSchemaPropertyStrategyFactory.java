package object.store.services.strategies.json.schema.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import org.springframework.stereotype.Component;

@Component
public class JsonSchemaPropertyStrategyFactory {

  private final Map<JsonSchemaPropertyStrategyName, JsonSchemaPropertyStrategy> strategies;

  public JsonSchemaPropertyStrategyFactory(Set<JsonSchemaPropertyStrategy> strategySet) {
    strategies = strategySet.stream().collect(
        Collectors.toMap(JsonSchemaPropertyStrategy::getStrategyName, o -> o, (prev, next) -> next, HashMap::new));
  }

  public JsonSchemaPropertyStrategy findStrategy(BasicBackendDefinitionDto basicBackendDefinitionDto) {
    return Optional.of(basicBackendDefinitionDto).map( dto -> switch(dto){
      case ArrayDefinitionDto ignored -> strategies.get(JsonSchemaPropertyStrategyName.ARRAY);
      case ObjectDefinitionDto ignored -> strategies.get(JsonSchemaPropertyStrategyName.OBJECT);
      case default -> strategies.get(JsonSchemaPropertyStrategyName.getMappedStrategyName(basicBackendDefinitionDto.getType()));
    }).orElseThrow();
  }
}
