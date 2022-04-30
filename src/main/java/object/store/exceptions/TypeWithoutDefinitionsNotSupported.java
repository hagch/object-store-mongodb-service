package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class TypeWithoutDefinitionsNotSupported extends InternalServerError {

  public TypeWithoutDefinitionsNotSupported(String typeName) {
    super("Type: " + typeName + "has no definitions");
  }
}
