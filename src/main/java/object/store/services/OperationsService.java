package object.store.services;


import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.CREATE;
import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.UPDATE;

import java.util.List;
import object.store.exceptions.DeleteObjectFailed;
import object.store.exceptions.OperationNotSupported;
import object.store.gen.mongodbservice.models.CreateUpdateOperationDefinition;
import object.store.gen.mongodbservice.models.DeleteOperationDefinition;
import object.store.gen.mongodbservice.models.OperationDefinition;
import object.store.gen.objects.models.Identifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OperationsService {

  private final ObjectsService objectsService;
  public OperationsService(ObjectsService objectsService){
    this.objectsService = objectsService;
  }
  public Mono<List<Object>> handleOperations(Flux<OperationDefinition> fluxOperations) {
    return fluxOperations.flatMap(operation -> switch (operation) {
      case CreateUpdateOperationDefinition casted && CREATE.equals(casted.getOperationType()) ->
          objectsService.createObjectByTypeIdentifier(
              Identifier.IDS, operation.getTypeReferenceId(),
              Mono.just(casted.getObject()));
      case DeleteOperationDefinition casted -> objectsService.deleteObjectByTypeIdentifier(Identifier.IDS,
              operation.getTypeReferenceId(), casted.getObjectId())
          .flatMap(deleteResult -> {
            if (deleteResult.wasAcknowledged()) {
              return Mono.just(deleteResult);
            }
            return Mono.error(new DeleteObjectFailed(casted.getObjectId()));
          });
      case CreateUpdateOperationDefinition casted && UPDATE.equals(casted.getOperationType()) ->
          objectsService.updateObjectByTypeIdentifier(Identifier.IDS,
              operation.getTypeReferenceId(),
              Mono.just(casted.getObject()));
      default -> Mono.error(new OperationNotSupported(operation.toString()));
    }).collectList();
  }
}
