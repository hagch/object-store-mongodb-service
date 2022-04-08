package object.store.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import object.store.daos.TypeDao;
import object.store.dtos.TypeDto;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.gen.mongodbservice.models.Type;
import object.store.mappers.TypeMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeService(TypeDao typeDao, TypeMapper mapper) {

  private static final List<BackendKeyType> typesToCheck = List.of(BackendKeyType.ONETOONE,BackendKeyType.ONETOMANY);

  public Mono<TypeDto> getById(String id) {
    return typeDao.getById(id);
  }

  public Mono<TypeDto> getByName(String name) {
    return typeDao.getByName(name);
  }

  public Flux<TypeDto> getAll() {
    return typeDao.getAll();
  }

  public Mono<TypeDto> createType(TypeDto document) {
    return validateTypeReferences(document).flatMap(typeDao::createType).flatMap(typeDao::createCollectionForType);
  }

  public Mono<TypeDto> updateById(TypeDto document) {
    return validateTypeReferences(document).flatMap(typeDao::updateTypeById);
  }

  public Mono<Void> delete(String id) {
    return typeDao.delete(id);
  }

  public Mono<TypeDto> deleteCollectionByTypeId(String id){
    return typeDao.getById(id).flatMap(type -> typeDao.deleteCollectionForType(type).thenReturn(type));
  }

  private Mono<TypeDto> validateTypeReferences(TypeDto type){
    return Mono.just(type).map(TypeDto::getBackendKeyDefinitions).flatMapMany( Flux::fromIterable)
        .filter( def -> typesToCheck.contains(def.getType())).flatMap( definitionToCheck -> {
          RelationDefinitionDto definition = (RelationDefinitionDto) definitionToCheck;
          return getById(definition.getReferencedTypeId())
              .switchIfEmpty(Mono.error(new IllegalStateException("referencedType does not exist")))
              .flatMap( referencedType -> {
                Optional<BasicBackendDefinitionDto> optionalKey = referencedType.getBackendKeyDefinitions().stream()
                    .filter(def -> def.getKey().equals(((RelationDefinitionDto) definitionToCheck).getReferenceKey())).findFirst();
                if(optionalKey.isEmpty()){
                  return Mono.empty().switchIfEmpty(Mono.error(new IllegalStateException("Key does not exist")));
                }
            return typeDao.getCollectionByName(referencedType.getName()).switchIfEmpty(Mono.error(new IllegalStateException("Collection does not exist")));
          });
        }).collectList().thenReturn(type);
  }
}
