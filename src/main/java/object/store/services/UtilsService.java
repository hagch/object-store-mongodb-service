package object.store.services;

import com.mongodb.BasicDBObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import object.store.constants.MongoConstants;
import object.store.daos.TypeDao;
import object.store.dtos.TypeDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.PrimitiveBackendDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.exceptions.CreateObjectsFailed;
import object.store.exceptions.ReferencedObjectNotFound;
import object.store.gen.mongodbservice.models.BackendKeyType;
import org.javatuples.Triplet;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UtilsService() {

  private static final List<BackendKeyType> typesToCheck = List.of(BackendKeyType.ONETOONE,BackendKeyType.ONETOMANY);

  public Mono<List<Map<String,Object>>> createObjects(TypeDto type, Flux<Map<String,Object>> fluxObjects,
      ReactiveMongoTemplate mongoTemplate,TypeDao typeDao){
    return fluxObjects.flatMap( object -> mapRequiredFieldsForSaveActions(Mono.just(type),Mono.just(object),true,
            typeDao,mongoTemplate)).map(
            Triplet::getValue0).collectList()
        .flatMap( objects -> getTypePrimaryKey(Mono.just(type)).flatMapMany( primaryKey -> mongoTemplate.insertAll(Mono.just(objects),
            type.getName()).map( object -> mapIdFieldForApi(object,primaryKey))).collectList()).switchIfEmpty(Mono.error(new CreateObjectsFailed(type.getName())));
  }

  public Mono<Triplet<Map<String, Object>, String, String>> mapRequiredFieldsForSaveActions(Mono<TypeDto> monoType,
      Mono<Map<String, Object>> monoObject,
      boolean generateId, TypeDao typeDao, ReactiveMongoTemplate mongoTemplate) {
    Mono<String> monoTypeName = monoType.map(TypeDto::getName);
    Mono<Map<String,Object>> validated =
        monoType.flatMap( type -> monoObject.flatMap(object -> validateObject(Flux.fromIterable(type.getBackendKeyDefinitions()),object,typeDao, mongoTemplate)));
    return validated.flatMap( object -> Mono.zip(getTypePrimaryKey(monoType), monoTypeName).map( tuple -> {
      var document = mapIdFieldForEntity(object,tuple.getT1(),generateId);
      return new Triplet<>(document, tuple.getT1(), tuple.getT2());
    }));
  }

  public Mono<Triplet<Map<String, Object>, String, String>> mapRequiredFieldsForSaveActions(Mono<TypeDto> monoType,
      Mono<Map<String, Object>> monoObject,
      boolean generateId, TypeService typeService, ReactiveMongoTemplate mongoTemplate) {
    Mono<String> monoTypeName = monoType.map(TypeDto::getName);
    Mono<Map<String,Object>> validated =
        monoType.flatMap( type -> monoObject.flatMap(object -> validateObject(Flux.fromIterable(type.getBackendKeyDefinitions()),object,typeService, mongoTemplate)));
    return validated.flatMap( object -> Mono.zip(getTypePrimaryKey(monoType), monoTypeName).map( tuple -> {
      var document = mapIdFieldForEntity(object,tuple.getT1(),generateId);
      return new Triplet<>(document, tuple.getT1(), tuple.getT2());
    }));
  }

  public Mono<String> getTypePrimaryKey(Mono<TypeDto> monoType) {
    return monoType.map(TypeDto::getBackendKeyDefinitions)
        .map(definitions ->
            definitions.stream().filter( definition -> definition instanceof PrimitiveBackendDefinitionDto).map( definition -> (PrimitiveBackendDefinitionDto) definition).filter(key -> BackendKeyType.PRIMARYKEY.equals(key.getType())).map(
                PrimitiveBackendDefinitionDto::getKey).findFirst()).mapNotNull(primaryKey -> primaryKey.orElse(null));
  }

  public Map<String, Object> mapIdFieldForEntity(Map<String, Object> object, String idName, boolean generateId) {
    object.put(MongoConstants.ID_NAME, generateId ? UUID.randomUUID().toString() : object.get(idName));
    object.remove(idName);
    return object;
  }

  public Map<String, Object> mapIdFieldForApi(Map<String, Object> object, String idName) {
    object.put(idName, object.get(MongoConstants.ID_NAME));
    object.remove(MongoConstants.ID_NAME);
    return object;
  }

  public Mono<Map<String,Object>> validateObject(Flux<BasicBackendDefinitionDto> definitions,
      Map<String,Object> object, TypeDao typeDao, ReactiveMongoTemplate mongoTemplate){
    return definitions.filter( def -> typesToCheck.contains(def.getType())).flatMap( definitionToCheck -> {
      // TODO ADD POSTGRES Solution
      RelationDefinitionDto definition = (RelationDefinitionDto) definitionToCheck;
      if(Objects.isNull(object.get(definition.getKey()))){
        return Mono.empty();
      }
      return typeDao.getById(definition.getReferencedTypeId()).flatMap( referencedType -> {
        BasicBackendDefinitionDto referencedDefinition =
            referencedType.getBackendKeyDefinitions().stream().filter( t -> t.getKey().equals(definition.getReferenceKey())).findFirst().orElse(
                null);
        Mono<BasicDBObject> referenceObject;

        if(BackendKeyType.PRIMARYKEY.equals(Objects.requireNonNull(referencedDefinition).getType())){
          referenceObject = mongoTemplate.findById(object.get(MongoConstants.ID_NAME), BasicDBObject.class,
              referencedType.getName());
        }else {
          referenceObject =
              mongoTemplate.findOne(Query.query(Criteria.where(referencedDefinition.getKey()).is(object.get(referencedDefinition.getKey()))),BasicDBObject.class,referencedType.getName());
        }
        return referenceObject.switchIfEmpty(Mono.error(new ReferencedObjectNotFound())).map( t -> object);
      });
    }).collectList().thenReturn(object);
  }

  public Mono<Map<String,Object>> validateObject(Flux<BasicBackendDefinitionDto> definitions,
      Map<String,Object> object, TypeService typeService, ReactiveMongoTemplate mongoTemplate){
    return definitions.filter( def -> typesToCheck.contains(def.getType())).flatMap( definitionToCheck -> {
      // TODO ADD POSTGRES Solution
      RelationDefinitionDto definition = (RelationDefinitionDto) definitionToCheck;
      if(Objects.isNull(object.get(definition.getKey()))){
        return Mono.empty();
      }
      return typeService.getById(definition.getReferencedTypeId()).flatMap( referencedType -> {
        BasicBackendDefinitionDto referencedDefinition =
            referencedType.getBackendKeyDefinitions().stream().filter( t -> t.getKey().equals(definition.getReferenceKey())).findFirst().orElse(
                null);
        Mono<BasicDBObject> referenceObject;

        if(BackendKeyType.PRIMARYKEY.equals(Objects.requireNonNull(referencedDefinition).getType())){
          referenceObject = mongoTemplate.findById(object.get(MongoConstants.ID_NAME), BasicDBObject.class,
              referencedType.getName());
        }else {
          referenceObject =
              mongoTemplate.findOne(Query.query(Criteria.where(referencedDefinition.getKey()).is(object.get(referencedDefinition.getKey()))),BasicDBObject.class,referencedType.getName());
        }
        return referenceObject.switchIfEmpty(Mono.error(new ReferencedObjectNotFound())).map( t -> object);
      });
    }).collectList().thenReturn(object);
  }
}
