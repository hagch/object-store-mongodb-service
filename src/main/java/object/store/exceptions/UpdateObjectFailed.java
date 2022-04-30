package object.store.exceptions;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

import object.store.exceptions.http.status.InternalServerError;

public class UpdateObjectFailed extends InternalServerError {

  public UpdateObjectFailed(String object, String type) {
    super("Could not update Object: " + object + " from type: " + type);
  }
}
