package org.example.orderservice.mapper;

import org.example.orderservice.dto.OrderRequestDTO;
import org.example.orderservice.dto.OrderItemRequestDTO;
import org.example.orderservice.dto.OrderItemResponseDTO;
import org.example.orderservice.dto.OrderResponseDTO;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", source = "orderItems")
    Order toEntity(OrderRequestDTO orderRequest);

    @Mapping(target = "userInfo", ignore = true)
    OrderResponseDTO toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(OrderItemRequestDTO orderItemRequest);

    @Mapping(target = "orderId", source = "order.id")
    OrderItemResponseDTO toResponse(OrderItem orderItem);
}