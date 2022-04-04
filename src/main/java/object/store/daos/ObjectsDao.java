package object.store.daos;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import object.store.constants.MongoConstants;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.gen.mongodbservice.models.PrimitiveDefinition;
import object.store.gen.mongodbservice.models.Type;
import object.store.services.TypeService;
import org.bson.Document;
import org.javatuples.Triplet;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public record ObjectsDao(TypeService typeService, ReactiveMongoTemplate mongoTemplate) {

  private static Map<String, Object> mapIdFieldForDocument(Tuple2<String, Document> tuple) {
    Document document = tuple.getT2();
    String idName = tuple.getT1();
    document.put(idName, document.get(MongoConstants.ID_NAME));
    document.remove(MongoConstants.ID_NAME);
    return document.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public Mono<Map<String, Object>> createObjectByTypeName(String typeName, Mono<Map<String, Object>> object) {
    Mono<Type> monoType = typeService.getByName(typeName);
    return createObjectByType(monoType, object);
  }

  public Mono<Map<String, Object>> createObjectByTypeId(String typeId, Mono<Map<String, Object>> object) {
    Mono<Type> monoType = typeService.getById(typeId);
    return createObjectByType(monoType, object);
  }

  public Mono<Map<String, Object>> getObjectByTypeName(String typeName, String objectId) {
    Mono<String> monoPrimaryKey = getTypePrimaryKey(typeService.getByName(typeName));
    return Mono.zip(monoPrimaryKey, mongoTemplate.findById(objectId, Document.class, typeName))
        .map(ObjectsDao::mapIdFieldForDocument);
  }

  public Mono<Map<String, Object>> getObjectByTypeId(String typeId, String objectId) {
    Mono<Type> monoType = typeService.getById(typeId);
    Mono<String> monoPrimaryKey = getTypePrimaryKey(monoType);
    return monoType.flatMap(type -> Mono.zip(monoPrimaryKey, mongoTemplate.findById(objectId, Document.class,
        type.getName()))).map(ObjectsDao::mapIdFieldForDocument);
  }

  public Mono<Map<String, Object>> updateObjectByTypeName(String typeName, Mono<Map<String, Object>> object) {
    Mono<Type> monoType = typeService.getByName(typeName);
    return updateObjectByType(monoType, object);
  }

  public Mono<Map<String, Object>> updateObjectByTypeId(String typeId, Mono<Map<String, Object>> object) {
    Mono<Type> monoType = typeService.getById(typeId);
    return updateObjectByType(monoType, object);
  }

  public Flux<Map<String, Object>> getAllObjectsByTypeName(String typeName) {
    Mono<Type> monoType = typeService.getByName(typeName);
    Mono<String> monoPrimaryKey = getTypePrimaryKey(monoType);
    return Flux.zip(monoPrimaryKey, mongoTemplate.findAll(Document.class, typeName))
        .map(ObjectsDao::mapIdFieldForDocument);
  }

  public Flux<Map<String, Object>> getAllObjectsByTypeId(String typeId) {
    Mono<Type> monoType = typeService.getById(typeId);
    Mono<String> monoPrimaryKey = getTypePrimaryKey(monoType);
    return monoType.flatMapMany(type -> Flux.zip(monoPrimaryKey, mongoTemplate.findAll(Document.class, type.getName()))
        .map(ObjectsDao::mapIdFieldForDocument));
  }

  private Mono<Map<String, Object>> createObjectByType(Mono<Type> monoType, Mono<Map<String, Object>> monoObject) {
    return mapRequiredFieldsForSaveActions(monoType, monoObject, true)
        .flatMap(triplet -> mongoTemplate.insert(triplet.getValue0(), triplet.getValue2())
            .map(savedObject -> mapIdFieldForApi(savedObject, triplet.getValue1())));
  }

  private Mono<Map<String, Object>> updateObjectByType(Mono<Type> monoType, Mono<Map<String, Object>> monoObject) {
    return mapRequiredFieldsForSaveActions(monoType, monoObject, false)
        .flatMap(triplet -> mongoTemplate.save(triplet.getValue0(), triplet.getValue2())
            .map(savedObject -> mapIdFieldForApi(savedObject, triplet.getValue1())));

  }

  private Mono<Triplet<Map<String, Object>, String, String>> mapRequiredFieldsForSaveActions(Mono<Type> monoType,
      Mono<Map<String, Object>> monoObject,
      boolean generateId) {
    Mono<String> monoTypeName = monoType.map(Type::getName);
    return Mono.zip(monoObject, getTypePrimaryKey(monoType), monoTypeName).map(tuple -> {
      var document = mapIdFieldForEntity(tuple.getT1(), tuple.getT2(), generateId);
      return new Triplet<>(document, tuple.getT2(), tuple.getT3());
    });
  }

  private Mono<String> getTypePrimaryKey(Mono<Type> monoType) {
    return monoType.map(Type::getBackendKeyDefinitions)
        .map(definitions ->
            definitions.stream().filter( definition -> definition instanceof PrimitiveDefinition).map( definition -> (PrimitiveDefinition) definition).filter(key -> BackendKeyType.PRIMARYKEY.equals(key.getType())).map(
            PrimitiveDefinition::getKey).findFirst()).mapNotNull(primaryKey -> primaryKey.orElse(null));
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
}
