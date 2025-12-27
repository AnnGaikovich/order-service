package org.example.orderservice.mapper;

import org.example.orderservice.dto.OrderRequestDTO;
import org.example.orderservice.dto.OrderResponseDTO;
import org.example.orderservice.entity.Order;
import org.example.orderservice.enums.OrderStatus;
import org.example.orderservice.exception.ValidationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // OrderRequestDTO -> Order
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderRequestDTO orderRequest);

    // Order -> OrderResponseDTO
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "userInfo", ignore = true)
    OrderResponseDTO toResponse(Order order);

    @Named("stringToStatus")
    default OrderStatus stringToStatus(String status) {
        if (status == null) return OrderStatus.PENDING;
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid order status: " + status);
        }
    }

    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        return status != null ? status.name() : null;
    }
}