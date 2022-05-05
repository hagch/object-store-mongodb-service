package object.store.entities.models;

import object.store.gen.mongodbservice.models.BackendKeyType;

public final class RelationDefinitionModel extends BasicBackendDefinitionModel {

  private String referencedTypeId;

  private String referenceKey;

  public RelationDefinitionModel(String key, Boolean isNullAble, BackendKeyType type, String referencedTypeId,
      String referenceKey, Boolean isUnique) {
    super(key, isNullAble, type, isUnique);
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
