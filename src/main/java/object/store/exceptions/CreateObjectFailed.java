package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class CreateObjectFailed extends InternalServerError {

  public CreateObjectFailed(String object, String type) {
    super("Could not create Object: " + object + " from type: " + type);
  }
}
