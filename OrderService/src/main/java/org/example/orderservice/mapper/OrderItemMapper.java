package org.example.orderservice.mapper;

import org.example.orderservice.dto.OrderItemRequestDTO;
import org.example.orderservice.dto.OrderItemResponseDTO;
import org.example.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(OrderItemRequestDTO orderItemRequest);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "itemName", ignore = true)
    @Mapping(target = "itemPrice", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    OrderItemResponseDTO toResponse(OrderItem orderItem);
}