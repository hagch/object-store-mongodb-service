package object.store.exceptions.http.status;

public abstract class InternalServerError extends RuntimeException{

  public InternalServerError(String message){
    super(message);
  }
}
