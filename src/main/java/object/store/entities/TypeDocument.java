package object.store.entities;

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
  private KeyDefinition[] keyDefinitions;

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

  public KeyDefinition[] getKeyDefinitions() {
    return keyDefinitions;
  }

  public void setKeyDefinitions(KeyDefinition[] keyDefinitions) {
    this.keyDefinitions = keyDefinitions;
  }
}
