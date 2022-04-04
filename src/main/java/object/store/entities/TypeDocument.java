package object.store.entities;

import java.util.List;
import object.store.entities.models.BasicBackendDefinitionModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("types")
public class TypeDocument {

  @Id
  private String id;
  @Indexed(unique = true)
  private String name;
  private Boolean additionalProperties;
  private List<BasicBackendDefinitionModel> backendKeyDefinitions;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(Boolean additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  public List<BasicBackendDefinitionModel> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<BasicBackendDefinitionModel> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
