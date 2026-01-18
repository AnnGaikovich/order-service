package org.example.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentEventDTO {
    private String eventId;
    private String eventType;
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String status;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}