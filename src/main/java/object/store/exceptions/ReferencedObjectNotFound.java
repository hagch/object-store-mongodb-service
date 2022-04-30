package object.store.exceptions;

import object.store.exceptions.http.status.NotFound;

public class ReferencedObjectNotFound extends NotFound {

  public ReferencedObjectNotFound() {
    super("Referenced Object not found");
  }
}
