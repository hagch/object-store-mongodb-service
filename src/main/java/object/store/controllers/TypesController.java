package object.store.controllers;

import object.store.gen.mongodbservice.apis.TypesApi;
import object.store.gen.mongodbservice.models.Type;
import object.store.mappers.TypeMapper;
import object.store.services.TypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public record TypesController(TypeService typeService, TypeMapper mapper) implements TypesApi {

  @Override
  public Mono<ResponseEntity<Type>> createType(Mono<Type> type, ServerWebExchange exchange) {
    return type.flatMap(typeService::createType).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteType(String id, ServerWebExchange exchange) {
    return typeService.deleteCollectionByTypeId(id).flatMap( type -> typeService.delete(type.getId())).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Type>> getTypeById(String id, ServerWebExchange exchange) {
    return typeService.getById(id).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Flux<Type>>> getTypes(ServerWebExchange exchange) {
    return typeService.getAll().map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Type>> updateTypeById(String id, Mono<Type> type, ServerWebExchange exchange) {
    return type.doOnNext(typeService::updateById).map(ResponseEntity::ok);
  }
}
