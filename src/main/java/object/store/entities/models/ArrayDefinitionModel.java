package object.store.entities.models;

import java.util.List;
import object.store.gen.mongodbservice.models.BackendKeyType;

public final class ArrayDefinitionModel extends BasicBackendDefinitionModel {

  private BackendKeyType primitiveArrayType;

  private List<BasicBackendDefinitionModel> properties;

  private Boolean additionalProperties;

  private Boolean additionalItems;

  public ArrayDefinitionModel(String key, Boolean isNullAble,
      BackendKeyType primitiveArrayType,
      List<BasicBackendDefinitionModel> properties, Boolean additionalProperties, Boolean additionalItems,
      Boolean isUnique) {
    super(key, isNullAble, BackendKeyType.ARRAY, isUnique);
    this.primitiveArrayType = primitiveArrayType;
    this.properties = properties;
    this.additionalProperties = additionalProperties;
    this.additionalItems = additionalItems;
  }

  public BackendKeyType getPrimitiveArrayType() {
    return primitiveArrayType;
  }

  public void setPrimitiveArrayType(BackendKeyType primitiveArrayType) {
    this.primitiveArrayType = primitiveArrayType;
  }

  public List<BasicBackendDefinitionModel> getProperties() {
    return properties;
  }

  public void setProperties(List<BasicBackendDefinitionModel> properties) {
    this.properties = properties;
  }

  public Boolean getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Boolean additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  public Boolean getAdditionalItems() {
    return additionalItems;
  }

  public void setAdditionalItems(Boolean additionalItems) {
    this.additionalItems = additionalItems;
  }
}

