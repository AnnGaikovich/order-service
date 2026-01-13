package org.example.orderservice.service;

import org.example.orderservice.dto.PaymentEventDTO;
import org.example.orderservice.entity.Order;
import org.example.orderservice.enums.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    @Transactional
    public void handlePaymentEvent(PaymentEventDTO event) {
        log.info("Received payment event: {}", event);

        if (!"CREATE_PAYMENT".equals(event.getEventType())) {
            log.warn("Unknown event type: {}", event.getEventType());
            return;
        }

        Long orderId = event.getOrderId();
        String paymentStatus = event.getStatus();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", orderId);
                    return new RuntimeException("Order not found with id: " + orderId);
                });

        // Обновляем статус заказа в зависимости от статуса платежа
        OrderStatus newOrderStatus;
        switch (paymentStatus) {
            case "SUCCESS":
                newOrderStatus = OrderStatus.PAID;
                break;
            case "FAILED":
                newOrderStatus = OrderStatus.PAYMENT_FAILED;
                break;
            case "CANCELLED":
                newOrderStatus = OrderStatus.CANCELLED;
                break;
            default:
                log.warn("Unknown payment status: {}", paymentStatus);
                return;
        }

        order.setStatus(newOrderStatus);
        orderRepository.save(order);

        log.info("Order {} status updated to {} due to payment event with status {}",
                orderId, newOrderStatus, paymentStatus);
    }
}