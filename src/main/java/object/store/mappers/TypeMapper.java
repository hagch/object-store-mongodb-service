package object.store.mappers;

import object.store.entities.TypeDocument;
import object.store.gen.mongodbservice.models.Type;
import object.store.mappers.api.KeyDefinitionApiMapper;
import object.store.mappers.dto.KeyDefinitionDtoMapper;
import object.store.mappers.entity.KeyDefinitionEntityMapper;
import object.store.dtos.TypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {KeyDefinitionEntityMapper.class, KeyDefinitionDtoMapper.class,
    KeyDefinitionApiMapper.class})
public interface TypeMapper {
  @Mapping(source = "backendKeyDefinitions", target = "backendKeyDefinitions")
  TypeDto entityToDto(TypeDocument document);
  @Mapping(source = "backendKeyDefinitions", target = "backendKeyDefinitions")
  TypeDocument dtoToEntity(TypeDto typeDto);
  @Mapping(source = "backendKeyDefinitions", target = "backendKeyDefinitions")
  TypeDto apiToDto(Type type);
  @Mapping(source = "backendKeyDefinitions", target = "backendKeyDefinitions")
  Type dtoToApi(TypeDto typeDto);

}
