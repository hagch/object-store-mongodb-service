package object.store.entities.models;

import java.util.List;
import object.store.gen.mongodbservice.models.BackendKeyType;

public class KeyDefinition {
  String key;
  BackendKeyType type;
  private Boolean isNullAble;
  private BackendKeyType primitiveArrayType;
  private List<KeyDefinition> properties;

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

  public Boolean getIsNullAble() {
    return isNullAble;
  }

  public void setIsNullAble(Boolean nullAble) {
    isNullAble = nullAble;
  }

  public BackendKeyType getPrimitiveArrayType() {
    return primitiveArrayType;
  }

  public void setPrimitiveArrayType(BackendKeyType primitiveArrayType) {
    this.primitiveArrayType = primitiveArrayType;
  }

  public List<KeyDefinition> getProperties() {
    return properties;
  }

  public void setProperties(List<KeyDefinition> properties) {
    this.properties = properties;
  }
}
