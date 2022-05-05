package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class WrongTypeId extends InternalServerError {

  public WrongTypeId() {
    super("Wrong Type Id in request");
  }
}
