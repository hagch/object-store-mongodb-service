package object.store.daos;

import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.transaction.Transactional;
import object.store.constants.MongoConstants;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.dtos.models.PrimitiveBackendDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.exceptions.CollectionCreationFailed;
import object.store.exceptions.CollectionNotFound;
import object.store.exceptions.CollectionOptionsGenerationFailed;
import object.store.exceptions.TypeNotFoundById;
import object.store.exceptions.TypeNotFoundByName;
import object.store.exceptions.TypeWithoutDefinitionsNotSupported;
import object.store.gen.mongodbservice.models.BackendKeyType;
import object.store.mappers.TypeMapper;
import object.store.repositories.TypeRepository;
import object.store.services.MongoJsonSchemaService;
import object.store.dtos.TypeDto;
import object.store.services.UtilsService;
import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
public record TypeDao(TypeRepository typeRepository, MongoJsonSchemaService mongoJsonSchemaService,
                      ReactiveMongoTemplate mongoTemplate, TypeMapper mapper, UtilsService utilsService) {

  public Flux<TypeDto> getAll() {
    return typeRepository.findAll().map(mapper::entityToDto);
  }

  public Mono<TypeDto> getById(String id) {
    return typeRepository.findById(id).switchIfEmpty(Mono.error(new TypeNotFoundById(id))).map(mapper::entityToDto);
  }

  public Mono<TypeDto> getByName(String name) {
    return typeRepository.findByName(name).switchIfEmpty(Mono.error(new TypeNotFoundByName(name))).map(mapper::entityToDto);
  }

  public Mono<TypeDto> createType(TypeDto document) {
    document.setId(UUID.randomUUID().toString());
    return typeRepository.save(mapper.dtoToEntity(document)).map(mapper::entityToDto);
  }

  @Transactional
  public Mono<TypeDto> updateType(TypeDto document, List<Map<String,Object>> objects) {
    return getById(document.getId())
        .flatMap(type -> mongoTemplate.dropCollection(type.getName()).thenReturn(type))
        .flatMap(type -> typeRepository.delete(mapper.dtoToEntity(type)).thenReturn(document))
        .flatMap(this::createCollectionForType)
        .flatMap(type -> typeRepository.save(mapper.dtoToEntity(type)))
        .flatMap( type -> utilsService.createObjects(mapper.entityToDto(type),Flux.fromIterable(objects),
                mongoTemplate,this)
            .thenReturn(mapper.entityToDto(type)));
  }

  public Mono<TypeDto> createCollectionForType(TypeDto document) {
    return Mono.just(document).flatMap(type -> {
      if (Objects.isNull(type.getBackendKeyDefinitions()) || type.getBackendKeyDefinitions().size() == 0) {
        return Mono.error(new TypeWithoutDefinitionsNotSupported(type.getName()));
      }
      return mongoJsonSchemaService.generateCollectionOptions(type)
          .switchIfEmpty(Mono.error(new CollectionOptionsGenerationFailed()))
          .map( collectionOptions -> Tuples.of(collectionOptions,type));
    }).flatMap(
        tuple -> mongoTemplate.createCollection(tuple.getT2().getName(), tuple.getT1())
            .switchIfEmpty(Mono.error(new CollectionCreationFailed()))
            .thenReturn(tuple.getT2())
    )
        .flatMap(typeDto ->
            createIndexes(Flux.fromIterable(typeDto.getBackendKeyDefinitions()),typeDto.getName()).thenReturn(typeDto)
            );
  }

  public Mono<Void> delete(String id) {
    return typeRepository.findById(id).switchIfEmpty(Mono.error(new TypeNotFoundById(id))).flatMap(document -> Mono.zip(typeRepository.delete(document),
        mongoTemplate.dropCollection(document.getName())).then());
  }

  public Mono<MongoCollection<Document>> getCollectionByName(String name){
    return mongoTemplate.getCollection(name).switchIfEmpty(Mono.error(new CollectionNotFound(
        name)));
  }

  private Mono<List<String>> createIndexes(Flux<BasicBackendDefinitionDto> definitionDtoFlux, String collectionName){
    return definitionDtoFlux.filter(dto -> !BackendKeyType.PRIMARYKEY.equals(dto.getType())).collectList()
        .flatMapMany( definitions -> Flux.fromIterable(getUniqueConstraintList(definitions)))
        .flatMap( definition -> mongoTemplate
            .indexOps(collectionName)
            .ensureIndex(new Index(definition.getKey(),Direction.ASC).unique()))
        .collectList();
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