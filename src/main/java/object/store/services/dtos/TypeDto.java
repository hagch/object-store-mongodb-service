package object.store.services.dtos;

import java.util.List;
import object.store.services.dtos.models.KeyDefinitionDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("types")
public class TypeDto {

  @Id
  private String id;
  @Indexed(unique = true)
  private String name;
  private boolean hasAdditionalProperties;
  private List<KeyDefinitionDto> backendKeyDefinitions;

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

  public List<KeyDefinitionDto> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<KeyDefinitionDto> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
