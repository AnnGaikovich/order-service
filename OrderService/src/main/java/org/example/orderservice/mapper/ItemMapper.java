package org.example.orderservice.mapper;

import org.example.orderservice.dto.ItemRequestDTO;
import org.example.orderservice.dto.ItemResponseDTO;
import org.example.orderservice.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Item toEntity(ItemRequestDTO itemRequest);

    ItemResponseDTO toResponse(Item item);
}