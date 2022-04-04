package object.store.dtos.models;

import java.util.List;
import object.store.gen.mongodbservice.models.BackendKeyType;

public non-sealed class ArrayDefinitionDto extends BasicBackendDefinitionDto{

  private BackendKeyType primitiveArrayType;

  private List<BasicBackendDefinitionDto> properties;

  private Boolean additionalProperties;

  private Boolean additionalItems;

  public ArrayDefinitionDto(String key, Boolean isNullAble,
      BackendKeyType primitiveArrayType,
      List<BasicBackendDefinitionDto> properties, Boolean additionalProperties, Boolean additionalItems) {
    super(key, isNullAble, BackendKeyType.ARRAY);
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

  public List<BasicBackendDefinitionDto> getProperties() {
    return properties;
  }

  public void setProperties(List<BasicBackendDefinitionDto> properties) {
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

