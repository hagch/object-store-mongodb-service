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
  private Boolean additionalProperties;
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

  public boolean getAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(boolean additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  public List<KeyDefinitionDto> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<KeyDefinitionDto> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
