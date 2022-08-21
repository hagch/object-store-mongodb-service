package object.store.controllers;

import javax.transaction.Transactional;
import object.store.gen.mongodbservice.apis.OperationsApi;
import object.store.gen.mongodbservice.models.OperationDefinition;
import object.store.mappers.TypeMapper;
import object.store.services.OperationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class OperationsController implements OperationsApi {

  private final OperationsService operationsService;

  public OperationsController(OperationsService operationsService){
    this.operationsService = operationsService;
  }
  @Override
  @Transactional
  public Mono<ResponseEntity<Void>> doOperations(Flux<OperationDefinition> transactionOperation,
      ServerWebExchange exchange) {
    return operationsService.handleOperations(transactionOperation).then().map(ResponseEntity::ok);
  }
}
