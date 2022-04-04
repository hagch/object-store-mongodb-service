package object.store.services.strategies.json.schema.property;

import java.util.HashMap;
import java.util.Map;
import object.store.gen.mongodbservice.models.BackendKeyType;

public enum JsonSchemaPropertyStrategyName {
  PRIMITIVE, ARRAY, OBJECT, ONE_TO_MANY, ONE_TO_ONE;

  private static final Map<BackendKeyType, JsonSchemaPropertyStrategyName> mapping = new HashMap<>() {{
    put(BackendKeyType.PRIMARYKEY, PRIMITIVE);
    put(BackendKeyType.BOOLEAN, PRIMITIVE);
    put(BackendKeyType.DOUBLE, PRIMITIVE);
    put(BackendKeyType.LONG, PRIMITIVE);
    put(BackendKeyType.INTEGER, PRIMITIVE);
    put(BackendKeyType.DATE, PRIMITIVE);
    put(BackendKeyType.TIMESTAMP, PRIMITIVE);
    put(BackendKeyType.STRING, PRIMITIVE);
  }};

  public static JsonSchemaPropertyStrategyName getMappedStrategyName(BackendKeyType backendKeyType) {
    return mapping.get(backendKeyType);
  }
}
