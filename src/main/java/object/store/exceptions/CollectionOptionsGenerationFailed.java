package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class CollectionOptionsGenerationFailed extends InternalServerError {

  public CollectionOptionsGenerationFailed() {
    super("Could not generate Collection Options");
  }
}
