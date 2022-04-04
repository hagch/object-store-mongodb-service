package object.store.dtos;

import java.util.List;
import object.store.dtos.models.BasicBackendDefinitionDto;
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
  private List<BasicBackendDefinitionDto> backendKeyDefinitions;

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

  public List<BasicBackendDefinitionDto> getBackendKeyDefinitions() {
    return backendKeyDefinitions;
  }

  public void setBackendKeyDefinitions(List<BasicBackendDefinitionDto> backendKeyDefinitions) {
    this.backendKeyDefinitions = backendKeyDefinitions;
  }
}
