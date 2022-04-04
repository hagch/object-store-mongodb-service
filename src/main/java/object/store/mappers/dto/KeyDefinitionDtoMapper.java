package object.store.mappers.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import object.store.dtos.models.ArrayDefinitionDto;
import object.store.dtos.models.BasicBackendDefinitionDto;
import object.store.dtos.models.ObjectDefinitionDto;
import object.store.dtos.models.PrimitiveBackendDefinitionDto;
import object.store.entities.models.ArrayDefinitionModel;
import object.store.entities.models.BasicBackendDefinitionModel;
import object.store.entities.models.ObjectDefinitionModel;
import object.store.entities.models.PrimitiveBackendDefinitionModel;
import object.store.gen.mongodbservice.models.ArrayDefinition;
import object.store.gen.mongodbservice.models.BasicBackendDefinition;
import object.store.gen.mongodbservice.models.ObjectDefinition;
import object.store.gen.mongodbservice.models.PrimitiveDefinition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeyDefinitionDtoMapper {

  default BasicBackendDefinition mapDtoToApi(BasicBackendDefinitionDto basicBackendDefinition){
    return mapDefinitionDtosToApi(List.of(basicBackendDefinition)).get(0);
  }

  default BasicBackendDefinitionModel mapDtoToEntity(BasicBackendDefinitionDto basicBackendDefinition){
    return mapDefinitionDtosToEntity(List.of(basicBackendDefinition)).get(0);
  }

  private List<BasicBackendDefinition> mapDefinitionDtosToApi(List<BasicBackendDefinitionDto> definitions){
    return definitions.stream().map( definition -> switch(definition){
      case ArrayDefinitionDto casted -> new ArrayDefinition().properties(mapDefinitionDtosToApi(casted.getProperties()))
          .primitiveArrayType(casted.getPrimitiveArrayType())
          .type(casted.getType())
          .additionalItems(casted.getAdditionalItems())
          .additionalProperties(casted.getAdditionalProperties())
          .key(casted.getKey())
          .isNullAble(casted.getIsNullAble());
      case ObjectDefinitionDto casted -> new ObjectDefinition()
          .properties(mapDefinitionDtosToApi(casted.getProperties()))
          .type(casted.getType())
          .additionalProperties(casted.getAdditionalProperties())
          .isNullAble(casted.getIsNullAble())
          .key(casted.getKey());
      case PrimitiveBackendDefinitionDto casted -> new PrimitiveDefinition()
          .type(casted.getType())
          .isNullAble(casted.getIsNullAble())
          .key(casted.getKey());
      default -> throw new IllegalStateException("Unexpected value: " + definition);
    }).collect(Collectors.toList());
  }

  private List<BasicBackendDefinitionModel> mapDefinitionDtosToEntity(List<BasicBackendDefinitionDto> definitions){
    return definitions.stream().map( definition -> switch(definition){
      case ArrayDefinitionDto casted -> new ArrayDefinitionModel(casted.getKey(),casted.getIsNullAble(),
          casted.getPrimitiveArrayType(),mapDefinitionDtosToEntity(casted.getProperties()),
          casted.getAdditionalProperties(),casted.getAdditionalItems());
      case ObjectDefinitionDto casted -> new ObjectDefinitionModel(casted.getKey(),casted.getIsNullAble()
         ,mapDefinitionDtosToEntity(casted.getProperties()),casted.getAdditionalProperties());
      case PrimitiveBackendDefinitionDto casted -> new PrimitiveBackendDefinitionModel(casted.getKey(),casted.getIsNullAble()
          ,casted.getType());
      default -> throw new IllegalStateException("Unexpected value: " + definition);
    }).collect(Collectors.toList());
  }
}
