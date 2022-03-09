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
  private boolean hasAdditionalProperties;
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

  public boolean isHasAdditionalProperties() {
    return hasAdditionalProperties;
  }

  public void setHasAdditionalProperties(boolean hasAdditionalProperties) {
    this.hasAdditionalProperties = hasAdditionalProperties;
  }

  public List<KeyDefinitionModel> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<KeyDefinitionModel> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
