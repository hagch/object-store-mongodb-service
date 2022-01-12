package object.store.services;

import com.mongodb.reactivestreams.client.MongoCollection;
import object.store.daos.TypeDao;
import object.store.entities.TypeDocument;
import object.store.gen.mongodbservice.models.Type;
import object.store.mappers.TypeMapper;
import org.bson.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeService(TypeDao typeDao, TypeMapper mapper) {

  public Mono<Type> getById(String id){
    return typeDao.getById(id).map(mapper::entityToApi);
  }

  public Mono<Type> getByName(String name) { return typeDao.getByName(name).map(mapper::entityToApi);}
  public Mono<Flux<Type>> getAll(){
    return Mono.just(typeDao.getAll().map(mapper::entityToApi));
  }

  public Mono<Type> createType(Type document){
    return typeDao.createType(mapper.apiToEntity(document)).flatMap(typeDao::createCollectionForType).map(mapper::entityToApi);
  }

  public Mono<Type> updateById(Type document){
    return typeDao.updateTypeById(mapper.apiToEntity(document)).map(mapper::entityToApi);
  }
}
