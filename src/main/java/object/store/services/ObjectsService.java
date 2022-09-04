package object.store.services;

import com.mongodb.client.result.DeleteResult;
import java.util.Map;
import object.store.daos.ObjectsDao;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record ObjectsService(ObjectsDao objectsDao) {

  public Mono<Map<String, Object>> createObjectByTypeId(String id,
      Mono<Map<String, Object>> object) {
    return objectsDao.createObjectByTypeId(id, object);
  }

  public Mono<DeleteResult> deleteObjectByTypeId(String id,
      String objectId) {
    return objectsDao.deleteObjectByTypeId(id, objectId);
  }

  public Mono<Map<String, Object>> getObjectByTypeId(String typeId, String objectId) {
    return objectsDao.getObjectByTypeId(typeId, objectId);
  }

  public Flux<Map<String, Object>> getObjectsByTypeId(String typeId) {
    return objectsDao.getAllObjectsByTypeId(typeId);
  }

  public Mono<Map<String, Object>> updateObjectById(String typeId, Mono<Map<String, Object>> monoObject) {
    return objectsDao.updateObjectByTypeId(typeId, monoObject);
  }
}
