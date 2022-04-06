package object.store.dtos.models;

import object.store.gen.mongodbservice.models.BackendKeyType;

public final class RelationDefinitionDto extends BasicBackendDefinitionDto{
  private String referencedTypeId;

  private String referenceKey;

  public RelationDefinitionDto(String key, Boolean isNullAble, BackendKeyType type, String referencedTypeId,
      String referenceKey) {
    super(key, isNullAble, type);
    this.referencedTypeId = referencedTypeId;
    this.referenceKey = referenceKey;
  }

  public String getReferencedTypeId() {
    return referencedTypeId;
  }

  public void setReferencedTypeId(String referencedTypeId) {
    this.referencedTypeId = referencedTypeId;
  }

  public String getReferenceKey() {
    return referenceKey;
  }

  public void setReferenceKey(String referenceKey) {
    this.referenceKey = referenceKey;
  }
}
