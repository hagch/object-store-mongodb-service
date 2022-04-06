package object.store.entities.models;

import java.util.List;
import object.store.gen.mongodbservice.models.BackendKeyType;

public final class ObjectDefinitionModel extends BasicBackendDefinitionModel {

  private List<BasicBackendDefinitionModel> properties;
  private Boolean additionalProperties;

  public ObjectDefinitionModel(String key, Boolean isNullAble,
      List<BasicBackendDefinitionModel> properties, Boolean additionalProperties) {
    super(key, isNullAble, BackendKeyType.OBJECT);
    this.properties = properties;
    this.additionalProperties = additionalProperties;
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

}

