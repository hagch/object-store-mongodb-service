package object.store.exceptions;

import object.store.exceptions.http.status.NotFound;

public class CollectionNotFound extends NotFound {

  public CollectionNotFound(String name) {
    super("Collection with name: " + name + " not found");
  }
}
