package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class ReferencedKeyDontExistsFromType extends InternalServerError {

  public ReferencedKeyDontExistsFromType(String key, String type) {
    super("Referenced key: " + key + " dont exists at type: " + type);
  }
}
