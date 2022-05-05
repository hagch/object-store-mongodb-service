package object.store.mappers.api;

import java.util.ArrayList;
import java.util.List;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.dtos.models.PrimitiveBackendDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.gen.mongodbservice.models.ArrayDefinition;
import object.store.gen.mongodbservice.models.BasicBackendDefinition;
import object.store.gen.mongodbservice.models.ObjectDefinition;
import object.store.gen.mongodbservice.models.PrimitiveDefinition;
import object.store.gen.mongodbservice.models.RelationDefinition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeyDefinitionApiMapper {

  default BasicBackendDefinitionDto mapApiToDto(BasicBackendDefinition basicBackendDefinition) {
    return mapDefinitionDtos(List.of(basicBackendDefinition)).get(0);
  }

  private List<BasicBackendDefinitionDto> mapDefinitionDtos(List<BasicBackendDefinition> definitions) {
    List<BasicBackendDefinitionDto> dtos = new ArrayList<>();
    for (BasicBackendDefinition definition : definitions) {
      dtos.add(switch (definition) {
        case ArrayDefinition casted -> new ArrayDefinitionDto(casted.getKey(), casted.getIsNullAble(),
            casted.getPrimitiveArrayType(), mapDefinitionDtos(casted.getProperties()),
            casted.getAdditionalProperties(), casted.getAdditionalItems(), casted.getIsUnique());
        case ObjectDefinition casted -> new ObjectDefinitionDto(casted.getKey(), casted.getIsNullAble(),
            mapDefinitionDtos(casted.getProperties()), casted.getAdditionalProperties(), casted.getIsUnique());
        case PrimitiveDefinition casted -> new PrimitiveBackendDefinitionDto(casted.getKey(), casted.getIsNullAble(),
            casted.getType(), casted.getIsUnique());
        case RelationDefinition casted -> new RelationDefinitionDto(casted.getKey(), casted.getIsNullAble(),
            casted.getType(), casted.getReferencedTypeId(), casted.getReferenceKey(), casted.getIsUnique());
        default -> throw new IllegalStateException("Unexpected value: " + definition);
      });
    }
    return dtos;
  }
}
