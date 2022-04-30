package object.store.controllers;

import object.store.gen.mongodbservice.apis.OperationsApi;
import object.store.gen.mongodbservice.models.OperationDefinition;
import object.store.mappers.TypeMapper;
import object.store.services.OperationsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public record OperationsController(OperationsService transactionService, TypeMapper mapper) implements OperationsApi {
  @Override
  public Mono<ResponseEntity<Void>> doOperations(Flux<OperationDefinition> transactionOperation,
      ServerWebExchange exchange) {
    return transactionService.handleOperations(transactionOperation).then().map(ResponseEntity::ok);
  }
}
