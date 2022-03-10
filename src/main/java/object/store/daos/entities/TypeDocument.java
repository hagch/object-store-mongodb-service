package object.store.daos.entities;

import java.util.List;
import object.store.daos.entities.models.KeyDefinitionModel;
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
  private List<KeyDefinitionModel> backendKeyDefinitions;

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

  public List<KeyDefinitionModel> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<KeyDefinitionModel> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
