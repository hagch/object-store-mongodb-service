package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class CollectionCreationFailed extends InternalServerError {

  public CollectionCreationFailed() {
    super("Could not create Collection");
  }
}
