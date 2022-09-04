package object.store.controllers;

import java.util.Map;
import object.store.exceptions.DeleteObjectFailed;
import object.store.gen.objects.apis.ObjectsApi;
import object.store.services.ObjectsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public record ObjectsController(ObjectsService objectsService) implements ObjectsApi {

  @Override
  public Mono<ResponseEntity<Map<String, Object>>> createObjectById(String id, Mono<Map<String, Object>> requestBody,
      ServerWebExchange exchange) {
    return objectsService.createObjectByTypeId(id, requestBody).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteObjectById(String id, String objectId, ServerWebExchange exchange) {
    return objectsService.deleteObjectByTypeId(id, objectId).flatMap(deleteResult -> {
      if (deleteResult.wasAcknowledged()) {
        return Mono.just(deleteResult);
      }
      return Mono.error(new DeleteObjectFailed(objectId));
    }).then().map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Map<String, Object>>> getObjectById(String id, String objectId,
      ServerWebExchange exchange) {
    return objectsService.getObjectByTypeId(id, objectId).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Flux<Map<String, Object>>>> getObjectsById(String id, ServerWebExchange exchange) {
    return Mono.just(objectsService.getObjectsByTypeId(id)).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Map<String, Object>>> updateObjectById(String id, String objectId,
      Mono<Map<String, Object>> requestBody, ServerWebExchange exchange) {
    return objectsService.updateObjectById(id, requestBody).map(ResponseEntity::ok);
  }
}
