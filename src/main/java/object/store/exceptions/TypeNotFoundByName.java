package object.store.exceptions;

import object.store.exceptions.http.status.NotFound;

public class TypeNotFoundByName extends NotFound {

  public TypeNotFoundByName(String name) {
    super("Type with name: " + name + " not found");
  }
}
