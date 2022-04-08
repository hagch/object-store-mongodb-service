package object.store.dtos.models;

import object.store.gen.mongodbservice.models.BackendKeyType;

/**
 * BasicBackendDefinition
 */

public final class PrimitiveBackendDefinitionDto extends BasicBackendDefinitionDto {

  public PrimitiveBackendDefinitionDto(String key, Boolean isNullAble, BackendKeyType type, Boolean isUnique) {
    super(key, isNullAble, type, isUnique);
  }
}

