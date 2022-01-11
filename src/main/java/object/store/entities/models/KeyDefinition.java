package object.store.entities.models;

import object.store.gen.mongodbservice.models.BackendKeyType;

public class KeyDefinition {
  String key;
  BackendKeyType type;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public BackendKeyType getType() {
    return type;
  }

  public void setType(BackendKeyType type) {
    this.type = type;
  }
}
