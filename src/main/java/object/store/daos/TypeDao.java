package object.store.daos;

import java.util.Objects;
import java.util.UUID;
import object.store.daos.entities.TypeDocument;
import object.store.mappers.TypeMapper;
import object.store.repositories.TypeRepository;
import object.store.services.MongoJsonSchemaService;
import object.store.services.dtos.TypeDto;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
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
      CollectionOptions options = mongoJsonSchemaService.generateCollectionOptions(type);
      return mongoTemplate.createCollection(type.getName(), options).thenReturn(type);
    });
  }

  public Mono<Void> delete(String id) {
    return typeRepository.findById(id).flatMap(document -> Mono.zip(typeRepository.delete(document),
        mongoTemplate.dropCollection(document.getName())).then());
  }
}