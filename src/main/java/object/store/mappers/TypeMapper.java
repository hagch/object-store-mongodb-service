package object.store.mappers;

import object.store.daos.entities.TypeDocument;
import object.store.gen.mongodbservice.models.Type;
import object.store.services.dtos.TypeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TypeMapper {

  TypeDto entityToDto(TypeDocument document);
  TypeDocument dtoToEntity(TypeDto typeDto);
  TypeDto apiToDto(Type type);
  Type dtoToApi(TypeDto typeDto);
}
