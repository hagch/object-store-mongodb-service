package object.store.daos;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.DeleteResult;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import object.store.constants.MongoConstants;
import object.store.dtos.TypeDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.PrimitiveBackendDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.exceptions.CreateObjectFailed;
import object.store.exceptions.CreateObjectsFailed;
import object.store.exceptions.ObjectNotFound;
import object.store.exceptions.ReferencedObjectNotFound;
import object.store.exceptions.TypeNotFoundById;
import object.store.exceptions.TypeNotFoundByName;
import object.store.exceptions.UpdateObjectFailed;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.services.TypeService;
import object.store.services.UtilsService;
import org.javatuples.Triplet;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public record ObjectsDao(TypeService typeService, ReactiveMongoTemplate mongoTemplate, UtilsService utilsService) {

  private static Map<String, Object> mapIdFieldForDocument(Tuple2<String, BasicDBObject> tuple) {
    BasicDBObject document = tuple.getT2();
    String idName = tuple.getT1();
    document.put(idName, document.get(MongoConstants.ID_NAME));
    document.remove(MongoConstants.ID_NAME);
    return document.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public Mono<DeleteResult> deleteObjectByTypeName(String typeName, String objectId){
    return mongoTemplate.remove(Query.query(Criteria.where(MongoConstants.ID_NAME).is(objectId)),typeName);
  }

  public Mono<DeleteResult> deleteObjectByTypeId(String typeId, String objectId){
    return typeService.getById(typeId).flatMap( type -> mongoTemplate.remove(Query.query(Criteria.where(MongoConstants.ID_NAME).is(objectId)),
        type.getName()));
  }
  public Mono<Map<String, Object>> createObjectByTypeName(String typeName, Mono<Map<String, Object>> object) {
    Mono<TypeDto> monoType = typeService.getByName(typeName);
    return createObjectByType(monoType, object);
  }

  public Mono<Map<String, Object>> createObjectByTypeId(String typeId, Mono<Map<String, Object>> object) {
    Mono<TypeDto> monoType = typeService.getById(typeId);
    return createObjectByType(monoType, object);
  }

  public Mono<Map<String, Object>> getObjectByTypeName(String typeName, String objectId) {
    return typeService.getByName(typeName)
        .switchIfEmpty(Mono.error(new TypeNotFoundByName(typeName)))
        .flatMap( type ->
            utilsService.getTypePrimaryKey(Mono.just(type))
                .flatMap( primaryKey -> mongoTemplate.findById(objectId, BasicDBObject.class,typeName)
                    .switchIfEmpty(Mono.error(new ObjectNotFound(objectId,typeName)))
                    .map( object -> Tuples.of(primaryKey,object))
                    .map(ObjectsDao::mapIdFieldForDocument)
                )
        );
  }

  public Mono<Map<String, Object>> getObjectByTypeId(String typeId, String objectId) {
    return typeService.getById(typeId).flatMap( type ->
        utilsService.getTypePrimaryKey(Mono.just(type))
            .flatMap( primaryKey -> mongoTemplate.findById(objectId, BasicDBObject.class,type.getName())
                .switchIfEmpty(Mono.error(new ObjectNotFound(objectId,typeId)))
                .map( object -> Tuples.of(primaryKey,object))
                .map(ObjectsDao::mapIdFieldForDocument)
            )
    );
  }

  public Mono<Map<String, Object>> updateObjectByTypeName(String typeName, Mono<Map<String, Object>> object) {
    Mono<TypeDto> monoType = typeService.getByName(typeName);
    return updateObjectByType(monoType, object);
  }

  public Mono<Map<String, Object>> updateObjectByTypeId(String typeId, Mono<Map<String, Object>> object) {
    Mono<TypeDto> monoType = typeService.getById(typeId);
    return updateObjectByType(monoType, object);
  }

  public Flux<Map<String, Object>> getAllObjectsByTypeName(String typeName) {
    Mono<TypeDto> monoType = typeService.getByName(typeName).switchIfEmpty(Mono.error(new TypeNotFoundByName(typeName)));
    Mono<String> monoPrimaryKey = utilsService.getTypePrimaryKey(monoType);
    return monoPrimaryKey.flatMapMany( key -> mongoTemplate.findAll(BasicDBObject.class,typeName).flatMap( object -> Mono.zip(Mono.just(key),Mono.just(object)))).map(ObjectsDao::mapIdFieldForDocument);
  }

  public Flux<Map<String, Object>> getAllObjectsByTypeId(String typeId) {
    Mono<TypeDto> monoType = typeService.getById(typeId).switchIfEmpty(Mono.error(new TypeNotFoundById(typeId)));
    Mono<String> monoPrimaryKey = utilsService.getTypePrimaryKey(monoType);
    return monoType.flatMapMany(type -> mongoTemplate.findAll(BasicDBObject.class,
        type.getName())).flatMap( object -> Mono.zip(monoPrimaryKey,Mono.just(object))).map(ObjectsDao::mapIdFieldForDocument).log();
  }

  private Mono<Map<String, Object>> createObjectByType(Mono<TypeDto> monoType, Mono<Map<String, Object>> monoObject) {
    return utilsService.mapRequiredFieldsForSaveActions(monoType, monoObject, true, typeService, mongoTemplate)
        .flatMap(triplet -> mongoTemplate.save(triplet.getValue0(), triplet.getValue2())
            .map(savedObject -> utilsService.mapIdFieldForApi(savedObject, triplet.getValue1())).switchIfEmpty(Mono.error(new CreateObjectFailed(triplet.getValue0().toString(),triplet.getValue2()))));
  }

  private Mono<Map<String, Object>> updateObjectByType(Mono<TypeDto> monoType, Mono<Map<String, Object>> monoObject) {
    return utilsService.mapRequiredFieldsForSaveActions(monoType, monoObject, false, typeService, mongoTemplate)
        .flatMap(triplet -> mongoTemplate.save(triplet.getValue0(), triplet.getValue2())
            .map(savedObject -> utilsService.mapIdFieldForApi(savedObject, triplet.getValue1()))
            .switchIfEmpty(Mono.error(new UpdateObjectFailed(triplet.getValue0().toString(),triplet.getValue2())))
        );
  }
}
