package object.store.dtos.models;

import java.util.List;
import object.store.gen.mongodbservice.models.BackendKeyType;

public final class ObjectDefinitionDto extends BasicBackendDefinitionDto{

  private List<BasicBackendDefinitionDto> properties;
  private Boolean additionalProperties;

  public ObjectDefinitionDto(String key, Boolean isNullAble,
      List<BasicBackendDefinitionDto> properties, Boolean additionalProperties, Boolean isUnique) {
    super(key, isNullAble, BackendKeyType.OBJECT, isUnique);
    this.properties = properties;
    this.additionalProperties = additionalProperties;
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
}

