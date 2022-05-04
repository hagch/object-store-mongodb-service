package object.store.services;


import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.CREATE;
import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.UPDATE;

import com.mongodb.client.result.DeleteResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import object.store.dtos.TypeDto;
import object.store.exceptions.DeleteObjectFailed;
import object.store.exceptions.NoOperationExecuted;
import object.store.exceptions.OperationNotSupported;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.gen.mongodbservice.models.CreateUpdateOperationDefinition;
import object.store.gen.mongodbservice.models.DeleteOperationDefinition;
import object.store.gen.mongodbservice.models.OperationDefinition;
import object.store.gen.objects.models.Identifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record OperationsService(ObjectsService objectsService, TypeService typeService) {

  @Transactional
  public Mono<List<Object>> handleOperations(Flux<OperationDefinition> fluxOperations) {
    return fluxOperations.flatMap(operation -> switch (operation) {
      case CreateUpdateOperationDefinition casted && CREATE.equals(casted.getOperationType()) -> objectsService.createObjectByTypeIdentifier(
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
      case CreateUpdateOperationDefinition casted && UPDATE.equals(casted.getOperationType()) -> objectsService.updateObjectByTypeIdentifier(Identifier.IDS,
              operation.getTypeReferenceId(),
              Mono.just(casted.getObject()));
      default -> Mono.error(new OperationNotSupported(operation.toString()));
    }).collectList();
  }
}
