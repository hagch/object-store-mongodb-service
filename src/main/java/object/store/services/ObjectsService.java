package object.store.services;

import java.util.Map;
import object.store.daos.ObjectsDao;
import object.store.gen.objects.models.Identifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record ObjectsService(ObjectsDao objectsDao) {

  public Mono<Map<String,Object>> createObjectByTypeIdentifier(Identifier identifierType, String identifier,
      Mono<Map<String,Object>> object){
    return switch (identifierType){
      case NAMES -> objectsDao.createObjectByTypeName(identifier,object);
      case IDS -> objectsDao.createObjectByTypeId(identifier,object);
    };
  }

  public Mono<Map<String,Object>> getObjectByTypeIdentifier(Identifier identifierType,
      String identifier,String objectId){
    return switch (identifierType){
      case NAMES -> objectsDao.getObjectByTypeName(identifier,objectId);
      case IDS -> objectsDao.getObjectByTypeId(identifier,objectId);
    };
  }

  public Mono<Map<String,Object>> updateObjectByTypeIdentifier(Identifier identifierType,
      String identifier,Mono<Map<String,Object>> monoObject){
    return switch (identifierType){
          case NAMES -> objectsDao.updateObjectByTypeName(identifier,monoObject);
          case IDS -> objectsDao.updateObjectByTypeId(identifier,monoObject);
        };
  }

  public Flux<Map<String,Object>> getObjectsByTypeIdentifier(Identifier identifierType, String identifier){
    return switch(identifierType){
      case NAMES -> objectsDao.getAllObjectsByTypeName(identifier);
      case IDS -> objectsDao.getAllObjectsByTypeId(identifier);
    };
  }
}
