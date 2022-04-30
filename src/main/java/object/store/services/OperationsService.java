package object.store.services;

import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.CREATE;
import static object.store.gen.mongodbservice.models.OperationDefinition.OperationTypeEnum.DELETE;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record OperationsService(ObjectsService objectsService, TypeService typeService) {

  public Mono<List<Object>> handleOperations(Flux<OperationDefinition> fluxOperations) {
    List<Supplier<Mono<?>>> rollBackActions = new ArrayList<>();
    return fluxOperations.flatMap(operation -> switch (operation) {
      case CreateUpdateOperationDefinition casted && CREATE.equals(casted.getOperationType()) -> objectsService.createObjectByTypeIdentifier(Identifier.IDS, operation.getTypeReferenceId(),
          Mono.just(casted.getObject())).map(object -> {
        rollBackActions.add(createDeleteRollback(operation.getTypeReferenceId(),object));
        return object;
      });
      case DeleteOperationDefinition casted -> objectsService.getObjectByTypeIdentifier(Identifier.IDS,
          casted.getTypeReferenceId(),
          casted.getObjectId()).flatMap(oldObject ->
          objectsService.deleteObjectByTypeIdentifier(Identifier.IDS,
              operation.getTypeReferenceId(),casted.getObjectId())
              .flatMap( deleteResult -> {
                if (deleteResult.wasAcknowledged()) {
                  rollBackActions.add(() -> objectsService.updateObjectByTypeIdentifier(Identifier.IDS,
                      operation.getTypeReferenceId(), Mono.just(oldObject)));
                } else {
                  return Mono.error(new DeleteObjectFailed(casted.getObjectId()));
                }
                return Mono.just(deleteResult);
              }));
      case CreateUpdateOperationDefinition casted && UPDATE.equals(casted.getOperationType()) -> getOldObject(operation.getTypeReferenceId(), casted.getObject()).flatMap(
          oldObject -> objectsService.updateObjectByTypeIdentifier(Identifier.IDS,
              operation.getTypeReferenceId(),
              Mono.just(casted.getObject())).map(result -> {
            rollBackActions.add(() -> objectsService.updateObjectByTypeIdentifier(Identifier.IDS,
                operation.getTypeReferenceId(), Mono.just(oldObject)));
            return result;
          }));
      default -> Mono.error(new OperationNotSupported(operation.toString()));
    }).onErrorResume( e -> doRollback(rollBackActions,Collections.emptyList()).then(Mono.error(e))).collectList().flatMap( results -> {
      if(results.isEmpty()){
        return Mono.error(new NoOperationExecuted());
      }
      return Mono.just(results);
    }).onErrorResume( e -> doRollback(rollBackActions,Collections.emptyList()).then(Mono.error(e)));
  }

  private Mono<Map<String, Object>> getOldObject(String typeId, Map<String, Object> newObject) {
    return typeService.getById(typeId).flatMapMany(type -> Flux.fromIterable(type.getBackendKeyDefinitions()))
        .filter(definition -> BackendKeyType.PRIMARYKEY.equals(definition.getType()))
        .next()
        .flatMap(primaryKey -> objectsService.getObjectByTypeIdentifier(Identifier.IDS, typeId,
            newObject.get(primaryKey.getKey()).toString()));
  }

  private Mono<List<Object>> doRollback(List<Supplier<Mono<?>>> rollBackActions, List<Object> results) {
    List<? extends Mono<?>> actions = rollBackActions.stream().map(Supplier::get).toList();
    return Flux.fromIterable(actions).collectList().thenReturn(results);
  }

  private Supplier<Mono<?>> createDeleteRollback(String typeId, Map<String,Object> object){
    return () -> typeService.getById(typeId).map(TypeDto::getBackendKeyDefinitions).flatMapMany(Flux::fromIterable)
        .filter( definition -> BackendKeyType.PRIMARYKEY.equals(definition.getType()))
        .next()
        .map( primaryDefinition -> object.get(primaryDefinition.getKey()))
        .flatMap(idValue -> objectsService.deleteObjectByTypeIdentifier(Identifier.IDS,typeId,(String) idValue));
  }
}
