package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class CollectionDeletionFailed extends InternalServerError {

  public CollectionDeletionFailed() {
    super("Could not delete Collection");
  }
}
