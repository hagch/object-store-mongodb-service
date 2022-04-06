package object.store.entities.models;

import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.gen.mongodbservice.models.BackendKeyType;

/**
 * BasicBackendDefinition
 */

public final class PrimitiveBackendDefinitionModel extends BasicBackendDefinitionModel {

  public PrimitiveBackendDefinitionModel(String key, Boolean isNullAble, BackendKeyType type) {
    super(key, isNullAble, type);
  }
}

