package object.store.daos;

import com.mongodb.BasicDBObject;
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
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.services.TypeService;
import org.bson.Document;
import org.javatuples.Triplet;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public record ObjectsDao(TypeService typeService, ReactiveMongoTemplate mongoTemplate) {

  private static final List<BackendKeyType> typesToCheck = List.of(BackendKeyType.ONETOONE,BackendKeyType.ONETOMANY);

  private static Map<String, Object> mapIdFieldForDocument(Tuple2<String, Document> tuple) {
    Document document = tuple.getT2();
    String idName = tuple.getT1();
    document.put(idName, document.get(MongoConstants.ID_NAME));
    document.remove(MongoConstants.ID_NAME);
    return document.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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
    Mono<String> monoPrimaryKey = getTypePrimaryKey(typeService.getByName(typeName));
    return Mono.zip(monoPrimaryKey, mongoTemplate.findById(objectId, Document.class, typeName))
        .map(ObjectsDao::mapIdFieldForDocument);
  }

  public Mono<Map<String, Object>> getObjectByTypeId(String typeId, String objectId) {
    Mono<TypeDto> monoType = typeService.getById(typeId);
    Mono<String> monoPrimaryKey = getTypePrimaryKey(monoType);
    return monoType.flatMap(type -> Mono.zip(monoPrimaryKey, mongoTemplate.findById(objectId, Document.class,
        type.getName()))).map(ObjectsDao::mapIdFieldForDocument);
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
    Mono<TypeDto> monoType = typeService.getByName(typeName);
    Mono<String> monoPrimaryKey = getTypePrimaryKey(monoType);
    return Flux.zip(monoPrimaryKey, mongoTemplate.findAll(Document.class, typeName))
        .map(ObjectsDao::mapIdFieldForDocument);
  }

  public Flux<Map<String, Object>> getAllObjectsByTypeId(String typeId) {
    Mono<TypeDto> monoType = typeService.getById(typeId);
    Mono<String> monoPrimaryKey = getTypePrimaryKey(monoType);
    return monoType.flatMapMany(type -> Flux.zip(monoPrimaryKey, mongoTemplate.findAll(Document.class, type.getName()))
        .map(ObjectsDao::mapIdFieldForDocument));
  }

  private Mono<Map<String, Object>> createObjectByType(Mono<TypeDto> monoType, Mono<Map<String, Object>> monoObject) {
    return mapRequiredFieldsForSaveActions(monoType, monoObject, true)
        .flatMap(triplet -> mongoTemplate.insert(triplet.getValue0(), triplet.getValue2())
            .map(savedObject -> mapIdFieldForApi(savedObject, triplet.getValue1())));
  }

  private Mono<Map<String, Object>> updateObjectByType(Mono<TypeDto> monoType, Mono<Map<String, Object>> monoObject) {
    return mapRequiredFieldsForSaveActions(monoType, monoObject, false)
        .flatMap(triplet -> mongoTemplate.save(triplet.getValue0(), triplet.getValue2())
            .map(savedObject -> mapIdFieldForApi(savedObject, triplet.getValue1())));

  }

  private Mono<Triplet<Map<String, Object>, String, String>> mapRequiredFieldsForSaveActions(Mono<TypeDto> monoType,
      Mono<Map<String, Object>> monoObject,
      boolean generateId) {
    Mono<String> monoTypeName = monoType.map(TypeDto::getName);
    Mono<Map<String,Object>> validatedObject =
        Mono.zip(monoType,monoObject).flatMap( tuple -> validateObject(Mono.just(tuple.getT1().getBackendKeyDefinitions()).flatMapMany(Flux::fromIterable),tuple.getT2()));
    return Mono.zip(validatedObject, getTypePrimaryKey(monoType), monoTypeName).map(tuple -> {
      var document = mapIdFieldForEntity(tuple.getT1(), tuple.getT2(), generateId);
      return new Triplet<>(document, tuple.getT2(), tuple.getT3());
    });
  }

  private Mono<String> getTypePrimaryKey(Mono<TypeDto> monoType) {
    return monoType.map(TypeDto::getBackendKeyDefinitions)
        .map(definitions ->
            definitions.stream().filter( definition -> definition instanceof PrimitiveBackendDefinitionDto).map( definition -> (PrimitiveBackendDefinitionDto) definition).filter(key -> BackendKeyType.PRIMARYKEY.equals(key.getType())).map(
                PrimitiveBackendDefinitionDto::getKey).findFirst()).mapNotNull(primaryKey -> primaryKey.orElse(null));
  }

  private Map<String, Object> mapIdFieldForEntity(Map<String, Object> object, String idName, boolean generateId) {
    object.put(MongoConstants.ID_NAME, generateId ? UUID.randomUUID().toString() : object.get(idName));
    object.remove(idName);
    return object;
  }

  private Map<String, Object> mapIdFieldForApi(Map<String, Object> object, String idName) {
    object.put(idName, object.get(MongoConstants.ID_NAME));
    object.remove(MongoConstants.ID_NAME);
    return object;
  }

  private Mono<Map<String,Object>> validateObject(Flux<BasicBackendDefinitionDto> definitions,
      Map<String,Object> object){
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
        return referenceObject.switchIfEmpty(Mono.error(new IllegalStateException("Referenced Object does not exist"))).map( t -> object);
      });
    }).collectList().thenReturn(object);
  }
}
