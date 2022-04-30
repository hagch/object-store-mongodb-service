package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class OperationNotSupported extends InternalServerError {

  public OperationNotSupported(String operation) {
    super("Operation no supported: " + operation);
  }
}
