package object.store.services;


import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.CREATE;
import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.UPDATE;

import java.util.List;
import java.util.Objects;
import object.store.exceptions.DeleteObjectFailed;
import object.store.exceptions.OperationNotSupported;
import object.store.gen.mongodbservice.models.CreateUpdateOperationDefinition;
import object.store.gen.mongodbservice.models.DeleteOperationDefinition;
import object.store.gen.mongodbservice.models.OperationDefinition;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OperationsService {

  private final ObjectsService objectsService;
  public OperationsService(ObjectsService objectsService){
    this.objectsService = objectsService;
  }

  @Transactional(rollbackFor = {UncategorizedMongoDbException.class})
  public Mono<List<Object>> handleOperations(Flux<OperationDefinition> fluxOperations) {
    return fluxOperations.collectList().flatMap(operations -> {
      if(CollectionUtils.isEmpty(operations)){
        return Mono.empty();
      }
      if(operations.size() > 1 ){
        return determineCall(operations.get(0)).flatMap( returnValue -> {
          operations.remove(0);
          return Flux.fromIterable(operations).flatMap(this::determineCall).collectList().map( list -> {
            list.add(returnValue);
            return list;
          });
        });
      }
      return Flux.from(determineCall(operations.get(0))).collectList();
    });
  }

  private Mono<Object> determineCall(OperationDefinition operation){
    return switch (operation) {
      case CreateUpdateOperationDefinition casted && CREATE.equals(casted.getOperationType()) ->
          objectsService.createObjectByTypeId(
              operation.getTypeReferenceId(),
              Mono.just(casted.getObject())).map( value -> (Object) value);
      case DeleteOperationDefinition casted -> objectsService.deleteObjectByTypeId(
              operation.getTypeReferenceId(), casted.getObjectId())
          .flatMap(deleteResult -> {
            if (deleteResult.wasAcknowledged()) {
              return Mono.just(deleteResult);
            }
            return Mono.error(new DeleteObjectFailed(casted.getObjectId()));
          });
      case CreateUpdateOperationDefinition casted && UPDATE.equals(casted.getOperationType()) ->
          objectsService.updateObjectById(
              operation.getTypeReferenceId(),
              Mono.just(casted.getObject())).map( value -> (Object) value);
      default -> Mono.error(new OperationNotSupported(operation.toString()));
    };
  }
}
