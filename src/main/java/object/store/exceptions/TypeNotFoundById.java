package object.store.exceptions;

import object.store.exceptions.http.status.NotFound;

public class TypeNotFoundById extends NotFound {

  public TypeNotFoundById(String typeId) {
    super("Type with id: " + typeId + " not found");
  }
}
