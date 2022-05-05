package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class CreateObjectsFailed extends InternalServerError {

  public CreateObjectsFailed(String type) {
    super("Could not create Objects from type: " + type);
  }
}
