package object.store.daos;

import java.util.Map;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import object.store.services.TypeService;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record ObjectsDao(TypeService typeService, ReactiveMongoTemplate mongoTemplate) {

  public Mono<Map<String,Object>> createObjectByTypeName(String typeName, Mono<Map<String,Object>> object){
    return object.map( document -> {
      document.put("_id",UUID.randomUUID().toString());
      document.remove("id");
      return document;
    }).flatMap(document -> mongoTemplate.insert(document,
        typeName).map( savedObject -> {
      Object idValue = savedObject.get("_id");
      savedObject.put("id",idValue);
      savedObject.remove("_id");
      return savedObject;
    }));
  }

  public Mono<Map<String,Object>> createObjectByTypeId(String typeId, Mono<Map<String,Object>> object){
    return typeService.getById(typeId).flatMap( type -> createObjectByTypeName(type.getName(),object));
  }

  public Mono<Map<String,Object>> getObjectByTypeName(String typeName, String objectId){
    return mongoTemplate.findById(objectId,Document.class,typeName).map( document -> {
      Object idValue = document.get("_id");
      document.remove("_id");
      document.put("id",idValue);
      return document.entrySet().stream().collect(Collectors.toMap(Entry::getKey,Entry::getValue));
    });
  }

  public Mono<Map<String,Object>> getObjectByTypeId(String typeId, String objectId){
    return typeService.getById(typeId).flatMap( type -> getObjectByTypeName(type.getName(),objectId));
  }

  public Mono<Map<String,Object>> updateObjectByTypeName(String typeName, Mono<Map<String,Object>> object){
    return object.map( document -> {
      Object idValue = document.get("id");
      document.remove("id");
      document.put("_id",idValue);
      return document;
    }).flatMap( document -> mongoTemplate.save(document,typeName)).map( document -> {
      Object idValue = document.get("_id");
      document.remove("_id");
      document.put("id",idValue);
      return document;
    });
  }

  public Mono<Map<String,Object>> updateObjectByTypeId(String typeId, Mono<Map<String,Object>> object){
    return typeService.getById(typeId).flatMap( type -> updateObjectByTypeName(type.getName(),object));
  }

  public Flux<Map<String,Object>> getAllObjectsByTypeName(String typeName){
    return  mongoTemplate.findAll(Document.class,typeName).map( document -> {
      Object idValue = document.get("_id");
      document.remove("_id");
      document.put("id",idValue);
      return document.entrySet().stream().collect(Collectors.toMap(Entry::getKey,Entry::getValue));
    });
  }

  public Flux<Map<String,Object>> getAllObjectsByTypeId(String typeId){
    return typeService.getById(typeId).flatMapMany( type -> getAllObjectsByTypeName(type.getName()));
  }
}
