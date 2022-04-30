package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class NoOperationExecuted extends InternalServerError {

  public NoOperationExecuted() {
    super("No Operations where executed");
  }
}
