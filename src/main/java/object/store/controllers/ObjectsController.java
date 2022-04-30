package object.store.controllers;

import java.util.Map;
import object.store.exceptions.DeleteObjectFailed;
import object.store.gen.objects.apis.ObjectsApi;
import object.store.gen.objects.models.Identifier;
import object.store.services.ObjectsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public record ObjectsController(ObjectsService objectsService) implements ObjectsApi {

  @Override
  public Mono<ResponseEntity<Map<String, Object>>> createObjectByTypeIdentifier(Identifier identifierType,
      String identifier,
      Mono<Map<String, Object>> requestBody, ServerWebExchange exchange) {
    return objectsService.createObjectByTypeIdentifier(identifierType, identifier, requestBody).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteObjectByTypeIdentifier(Identifier identifierType, String identifier,
      String objectId, ServerWebExchange exchange) {
    return objectsService.deleteObjectByTypeIdentifier(identifierType, identifier, objectId).flatMap( deleteResult -> {
      if(deleteResult.wasAcknowledged()){
        return Mono.just(deleteResult);
      }
      return Mono.error(new DeleteObjectFailed(objectId));
    }).then().map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Map<String, Object>>> getObjectByTypeIdentifier(Identifier identifierType,
      String identifier, String objectId, ServerWebExchange exchange) {
    return objectsService.getObjectByTypeIdentifier(identifierType, identifier, objectId).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Flux<Map<String, Object>>>> getObjectsByTypeIdentifier(Identifier identifierType,
      String identifier,
      ServerWebExchange exchange) {
    return Mono.just(objectsService.getObjectsByTypeIdentifier(identifierType, identifier)).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Map<String, Object>>> updateObjectByTypeIdentifier(Identifier identifierType,
      String identifier, String objectId, Mono<Map<String, Object>> requestBody, ServerWebExchange exchange) {
    return objectsService.updateObjectByTypeIdentifier(identifierType, identifier, requestBody).map(ResponseEntity::ok);
  }
}
