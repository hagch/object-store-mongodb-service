package object.store.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import object.store.daos.TypeDao;
import object.store.dtos.TypeDto;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.exceptions.CollectionNotFound;
import object.store.exceptions.ReferencedKeyDontExistsFromType;
import object.store.exceptions.TypeNotFoundById;
import object.store.exceptions.TypeNotFoundByName;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.gen.mongodbservice.models.Type;
import object.store.mappers.TypeMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeService(TypeDao typeDao, TypeMapper mapper) {

  private static final List<BackendKeyType> typesToCheck = List.of(BackendKeyType.ONETOONE,BackendKeyType.ONETOMANY,
      BackendKeyType.ARRAY, BackendKeyType.OBJECT);

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

  private Mono<TypeDto> validateTypeReferences(TypeDto type){
    return checkFoundRelation(Mono.just(type).map(TypeDto::getBackendKeyDefinitions).flatMapMany( Flux::fromIterable)).collectList().thenReturn(type);
  }

  private Flux<Void> checkFoundRelation(Flux<BasicBackendDefinitionDto> fluxDefinition){
    return fluxDefinition.filter( def -> typesToCheck.contains(def.getType())).flatMap( definitionToCheck -> switch(definitionToCheck){
      case RelationDefinitionDto definition -> validateRelation(definition);
      case ObjectDefinitionDto definition && !definition.getProperties().isEmpty()->  checkFoundRelation(Flux.fromIterable(definition.getProperties()).thenMany(Flux.empty()));
      case ArrayDefinitionDto definition && !definition.getProperties().isEmpty() -> checkFoundRelation(Flux.fromIterable(definition.getProperties())).thenMany(Flux.empty());
      case default -> Flux.empty();
    });
  }
  private Flux<Void> validateRelation(RelationDefinitionDto relation){
    return Flux.concat(getById(relation.getReferencedTypeId())
        .flatMap( referencedType -> {
          Optional<BasicBackendDefinitionDto> optionalKey = findDefinition(relation.getReferenceKey(),
              referencedType.getBackendKeyDefinitions());
          if(optionalKey.isEmpty()){
            return Mono.error(new ReferencedKeyDontExistsFromType(relation.getReferenceKey(), referencedType.getName()));
          }
          return typeDao.getCollectionByName(referencedType.getName());
        }).thenMany(Flux.empty()));
  }

  private Optional<BasicBackendDefinitionDto> findDefinition(String key,
      List<BasicBackendDefinitionDto> definitions){
    Optional<BasicBackendDefinitionDto> found = definitions.stream().filter( definition -> Objects.equals(key,
        definition.getKey())).findFirst();
    if(found.isEmpty()){
      Optional<Optional<BasicBackendDefinitionDto>> optional =
          definitions.stream().filter( definitionDto -> definitionDto instanceof ArrayDefinitionDto || definitionDto instanceof ObjectDefinitionDto)
          .map( definition -> {
            if(definition instanceof  ArrayDefinitionDto casted && Objects.nonNull(casted.getProperties()) && !casted.getProperties().isEmpty()){
              return casted.getProperties();
            }
            if(definition instanceof ObjectDefinitionDto casted && Objects.nonNull(casted.getProperties())){
              return casted.getProperties();
            }
            return Collections.emptyList();
          }).map( foundDefinitions -> findDefinition(key, (List<BasicBackendDefinitionDto>) foundDefinitions)).filter(
                  Optional::isPresent).findFirst();
      found = optional.orElse(found);
    }
    return found;
  }
}
