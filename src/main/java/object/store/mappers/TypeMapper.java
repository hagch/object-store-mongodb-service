package object.store.mappers;

import object.store.entities.TypeDocument;
import object.store.gen.mongodbservice.models.Type;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TypeMapper {

  Type entityToApi(TypeDocument document);
  TypeDocument apiToEntity(Type type);
}
