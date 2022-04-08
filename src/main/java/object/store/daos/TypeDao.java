package object.store.daos;

import com.mongodb.reactivestreams.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.mappers.TypeMapper;
import object.store.repositories.TypeRepository;
import object.store.services.MongoJsonSchemaService;
import object.store.dtos.TypeDto;
import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record TypeDao(TypeRepository typeRepository, MongoJsonSchemaService mongoJsonSchemaService,
                      ReactiveMongoTemplate mongoTemplate, TypeMapper mapper) {

  public Flux<TypeDto> getAll() {
    return typeRepository.findAll().map(mapper::entityToDto);
  }

  public Mono<TypeDto> getById(String id) {
    return typeRepository.findById(id).map(mapper::entityToDto);
  }

  public Mono<TypeDto> getByName(String name) {
    return typeRepository.findByName(name).map(mapper::entityToDto);
  }

  public Mono<TypeDto> createType(TypeDto document) {
    document.setId(UUID.randomUUID().toString());
    return typeRepository.save(mapper.dtoToEntity(document)).map(mapper::entityToDto);
  }

  public Mono<TypeDto> updateTypeById(TypeDto document) {
    return typeRepository.save(mapper.dtoToEntity(document)).map(mapper::entityToDto);
  }

  public Mono<TypeDto> createCollectionForType(TypeDto document) {
    return Mono.just(document).flatMap(type -> {
      if (Objects.isNull(type.getBackendKeyDefinitions()) || type.getBackendKeyDefinitions().size() == 0) {
        return Mono.error(new IllegalArgumentException());
      }
      return Mono.zip(mongoJsonSchemaService.generateCollectionOptions(type), Mono.just(type));
    }).flatMap(
        tuple -> mongoTemplate.createCollection(tuple.getT2().getName(), tuple.getT1()).thenReturn(tuple.getT2())
    )
        .flatMap(typeDto ->
            createIndexes(Flux.fromIterable(typeDto.getBackendKeyDefinitions()),typeDto.getName()).thenReturn(typeDto)
            );
  }

  public Mono<Void> deleteCollectionForType(TypeDto document) {
    return mongoTemplate.dropCollection(document.getName());
  }

  public Mono<Void> delete(String id) {
    return typeRepository.findById(id).flatMap(document -> Mono.zip(typeRepository.delete(document),
        mongoTemplate.dropCollection(document.getName())).then());
  }

  public Mono<MongoCollection<Document>> getCollectionByName(String name){
    return mongoTemplate.getCollection(name);
  }

  private Mono<List<String>> createIndexes(Flux<BasicBackendDefinitionDto> definitionDtoFlux, String collectionName){
    return definitionDtoFlux.collectList()
        .flatMapMany( definitions -> Flux.fromIterable(getUniqueConstraintList(definitions)))
        .flatMap( definition -> mongoTemplate
            .indexOps(collectionName)
            .ensureIndex(new Index(definition.getKey(),Direction.ASC).unique()))
        .collectList().log();
  }

  private List<BasicBackendDefinitionDto> getUniqueConstraintList(
      List<BasicBackendDefinitionDto> definitions) {
    List<BasicBackendDefinitionDto> mappedList = new ArrayList<>();
    for (BasicBackendDefinitionDto definition : definitions) {
      if(Boolean.TRUE.equals(definition.getIsUnique())){
        mappedList.add(definition);
      }
      if(definition instanceof ArrayDefinitionDto casted && Objects.nonNull(casted.getProperties()) && !casted.getProperties().isEmpty()){
        List<BasicBackendDefinitionDto> mappedKeys = casted.getProperties().stream().map(def -> {
          def.setKey(definition.getKey().concat(".").concat(def.getKey()));
          return def;
        }).toList();
        mappedList.addAll(getUniqueConstraintList(mappedKeys));
      }
      if(definition instanceof ObjectDefinitionDto casted && Objects.nonNull(casted.getProperties()) && !casted.getProperties().isEmpty()){
        List<BasicBackendDefinitionDto> mappedKeys = casted.getProperties().stream().map(def -> {
          def.setKey(definition.getKey().concat(".").concat(def.getKey()));
          return def;
        }).toList();
        mappedList.addAll(getUniqueConstraintList(mappedKeys));
      }
    }
    return mappedList;
  }
}