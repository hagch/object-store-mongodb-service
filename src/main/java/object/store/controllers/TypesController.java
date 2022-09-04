package object.store.controllers;

import object.store.gen.mongodbservice.apis.TypesApi;
import object.store.gen.mongodbservice.models.Type;
import object.store.gen.mongodbservice.models.UpdateType;
import object.store.mappers.TypeMapper;
import object.store.services.TypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TypesController implements TypesApi {

  private final TypeService typeService;

  private final TypeMapper mapper;

  public TypesController(TypeService typeService, TypeMapper mapper) {
    this.typeService = typeService;
    this.mapper = mapper;
  }

  @Override
  public Mono<ResponseEntity<Type>> createType(Mono<Type> type, ServerWebExchange exchange) {
    return type.flatMap(typeToCreate -> typeService.createType(mapper.apiToDto(typeToCreate))).map(mapper::dtoToApi
    ).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteType(String id, ServerWebExchange exchange) {
    return typeService.delete(id).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Type>> getTypeById(String id, ServerWebExchange exchange) {
    return typeService.getById(id).map(mapper::dtoToApi).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Flux<Type>>> getTypes(ServerWebExchange exchange) {
    return Mono.just(typeService.getAll().map(mapper::dtoToApi)).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Type>> updateType(String id, Mono<UpdateType> updateType,
      ServerWebExchange exchange) {
    return updateType.flatMap(typeUpdate -> typeService.updateType(id, mapper.apiToDto(typeUpdate.getType()),
        typeUpdate.getObjects())).map(mapper::dtoToApi).map(ResponseEntity::ok);
  }
}
