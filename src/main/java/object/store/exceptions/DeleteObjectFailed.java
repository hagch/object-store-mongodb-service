package object.store.exceptions;

import object.store.exceptions.http.status.InternalServerError;

public class DeleteObjectFailed extends InternalServerError {

  public DeleteObjectFailed(String id) {
    super("Could not delete Object with id: " + id);
  }
}
