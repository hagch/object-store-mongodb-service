package object.store.entities;

import java.util.List;
import object.store.entities.models.KeyDefinition;
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
  private List<KeyDefinition> backendKeyDefinitions;

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

  public List<KeyDefinition> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<KeyDefinition> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
