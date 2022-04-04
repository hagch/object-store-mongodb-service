package object.store.dtos.models;

import object.store.gen.mongodbservice.models.BackendKeyType;

/**
 * BasicBackendDefinition
 */

public non-sealed class PrimitiveBackendDefinitionDto extends BasicBackendDefinitionDto {

  public PrimitiveBackendDefinitionDto(String key, Boolean isNullAble, BackendKeyType type) {
    super(key, isNullAble, type);
  }
}

