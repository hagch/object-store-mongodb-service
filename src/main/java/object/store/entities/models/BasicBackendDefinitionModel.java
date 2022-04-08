package object.store.entities.models;

import object.store.gen.mongodbservice.models.BackendKeyType;

/**
 * BasicBackendDefinition
 */

public sealed class BasicBackendDefinitionModel permits ArrayDefinitionModel, ObjectDefinitionModel,
    PrimitiveBackendDefinitionModel, RelationDefinitionModel{

  private String key;

  private Boolean isNullAble;

  private BackendKeyType type;

  private Boolean isUnique;

  public BasicBackendDefinitionModel(String key, Boolean isNullAble, BackendKeyType type, Boolean isUnique) {
    this.key = key;
    this.isNullAble = isNullAble;
    this.type = type;
    this.isUnique = isUnique;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Boolean getIsNullAble() {
    return isNullAble;
  }

  public void setIsNullAble(Boolean isNullAble) {
    this.isNullAble = isNullAble;
  }

  public BackendKeyType getType() {
    return type;
  }

  public void setType(BackendKeyType type) {
    this.type = type;
  }

  public Boolean getIsUnique() {
    return isUnique;
  }

  public void setIsUnique(Boolean unique) {
    isUnique = unique;
  }
}

