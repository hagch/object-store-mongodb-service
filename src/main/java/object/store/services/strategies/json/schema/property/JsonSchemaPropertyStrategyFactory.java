package object.store.services.strategies.json.schema.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import object.store.gen.mongodbservice.models.BackendKeyType;
import org.springframework.stereotype.Component;

@Component
public class JsonSchemaPropertyStrategyFactory {

  private final Map<JsonSchemaPropertyStrategyName, JsonSchemaPropertyStrategy> strategies;

  public JsonSchemaPropertyStrategyFactory(Set<JsonSchemaPropertyStrategy> strategySet) {
    strategies = strategySet.stream().collect(
        Collectors.toMap(JsonSchemaPropertyStrategy::getStrategyName, o -> o, (prev, next) -> next, HashMap::new));
  }

  public JsonSchemaPropertyStrategy findStrategy(BackendKeyType type) {
    return Optional.of(strategies.get(JsonSchemaPropertyStrategyName.getMappedStrategyName(type))).orElseThrow();
  }
}
