package object.store.advices;

import object.store.exceptions.http.status.InternalServerError;
import object.store.exceptions.http.status.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(NotFound.class)
  Mono<ResponseEntity<NotFound>> notFound(NotFound exception) {
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception));
  }

  @ExceptionHandler(InternalServerError.class)
  Mono<ResponseEntity<InternalServerError>> internalError(InternalServerError exception) {
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception));
  }
}
