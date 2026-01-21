package org.example.orderservice.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDTO {

   private String status;
   private List<OrderItemRequestDTO> orderItems;
}