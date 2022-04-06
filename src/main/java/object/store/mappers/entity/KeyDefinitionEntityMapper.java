package object.store.mappers.entity;

import java.util.List;
import java.util.stream.Collectors;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.dtos.models.PrimitiveBackendDefinitionDto;
import object.store.dtos.models.RelationDefinitionDto;
import object.store.entities.models.ArrayDefinitionModel;
import object.store.entities.models.BasicBackendDefinitionModel;
import object.store.entities.models.ObjectDefinitionModel;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.entities.models.PrimitiveBackendDefinitionModel;
import object.store.entities.models.RelationDefinitionModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeyDefinitionEntityMapper {

  default BasicBackendDefinitionDto mapEntityToDto(BasicBackendDefinitionModel basicBackendDefinition){
    return mapEntitiesToDtos(List.of(basicBackendDefinition)).get(0);
  }

  private List<BasicBackendDefinitionDto> mapEntitiesToDtos(List<BasicBackendDefinitionModel> definitions){
    return definitions.stream().map( definition -> switch(definition){
      case ArrayDefinitionModel casted -> new ArrayDefinitionDto(casted.getKey(),casted.getIsNullAble(),
          casted.getPrimitiveArrayType(),mapEntitiesToDtos(casted.getProperties()),
          casted.getAdditionalProperties(),casted.getAdditionalItems());
      case ObjectDefinitionModel casted -> new ObjectDefinitionDto(casted.getKey(),casted.getIsNullAble()
          ,mapEntitiesToDtos(casted.getProperties()),casted.getAdditionalProperties());
      case PrimitiveBackendDefinitionModel casted -> new PrimitiveBackendDefinitionDto(casted.getKey(),casted.getIsNullAble()
          ,casted.getType());
      case RelationDefinitionModel casted -> new RelationDefinitionDto(casted.getKey(),casted.getIsNullAble()
          ,casted.getType(), casted.getReferencedTypeId(), casted.getReferenceKey());
      default -> throw new IllegalStateException("Unexpected value: " + definition);
    }).collect(Collectors.toList());
  }
}
