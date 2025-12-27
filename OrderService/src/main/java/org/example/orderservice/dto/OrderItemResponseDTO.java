package org.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponseDTO {
    private Long orderId;
    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}