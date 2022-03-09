package object.store.services;

import object.store.daos.TypeDao;
import object.store.gen.mongodbservice.models.Type;
import object.store.mappers.TypeMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeService(TypeDao typeDao, TypeMapper mapper) {

  public Mono<Type> getById(String id) {
    return typeDao.getById(id).map(mapper::dtoToApi);
  }

  public Mono<Type> getByName(String name) {
    return typeDao.getByName(name).map(mapper::dtoToApi);
  }

  public Mono<Flux<Type>> getAll() {
    return Mono.just(typeDao.getAll().map(mapper::dtoToApi));
  }

  public Mono<Type> createType(Type document) {
    return typeDao.createType(mapper.apiToDto(document)).flatMap(typeDao::createCollectionForType)
        .map(mapper::dtoToApi);
  }

  public Mono<Type> updateById(Type document) {
    return typeDao.updateTypeById(mapper.apiToDto(document)).map(mapper::dtoToApi);
  }

  public Mono<Void> delete(String id) {
    return typeDao.delete(id);
  }
}
