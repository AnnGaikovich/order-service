package org.example.orderservice.service;

import org.example.orderservice.dto.OrderItemRequestDTO;
import org.example.orderservice.dto.OrderRequestDTO;
import org.example.orderservice.dto.OrderResponseDTO;
import org.example.orderservice.dto.UserInfoDTO;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderItem;
import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.repository.ItemRepository;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.specification.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserInfoService userInfoService;
    private final ItemRepository itemRepository;

    @Transactional
    public OrderResponseDTO createOrder(Long userId, OrderRequestDTO orderRequest) {
        log.info("Creating order for user from token: {}", userId);

        // Проверяем существование товаров
        if (orderRequest.getOrderItems() != null) {
            for (OrderItemRequestDTO itemRequest : orderRequest.getOrderItems()) {
                if (!itemRepository.existsById(itemRequest.getItemId())) {
                    throw new RuntimeException("Item not found with id: " + itemRequest.getItemId());
                }
            }
        }

        Order order = orderMapper.toEntity(orderRequest);
        order.setUserId(userId); // Сохраняем userId из токена

        if (orderRequest.getOrderItems() != null) {
            List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                    .map(orderMapper::toEntity)
                    .peek(orderItem -> orderItem.setOrder(order))
                    .collect(Collectors.toList());
            order.setOrderItems(orderItems);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {}", savedOrder.getId());

        return enrichOrderWithUserInfo(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return enrichOrderWithUserInfo(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByIdAndUser(Long orderId, Long userId) {
        log.info("Fetching order with id: {} for user: {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserIdAndDeletedFalse(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId + " for user: " + userId));
        return enrichOrderWithUserInfo(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdAndDeletedFalse(userId);
        return orders.stream()
                .map(this::enrichOrderWithUserInfo)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO orderRequest) {
        log.info("Updating order with id: {}", id);

        Order existingOrder = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (orderRequest.getStatus() != null) {
            existingOrder.setStatus(orderRequest.getStatus());
        }
        if (orderRequest.getTotalPrice() != null) {
            existingOrder.setTotalPrice(orderRequest.getTotalPrice());
        }

        if (orderRequest.getOrderItems() != null) {
            existingOrder.getOrderItems().clear();
            List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                    .map(orderMapper::toEntity)
                    .peek(orderItem -> orderItem.setOrder(existingOrder))
                    .collect(Collectors.toList());
            existingOrder.setOrderItems(orderItems);
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with id: {}", id);

        return enrichOrderWithUserInfo(updatedOrder);
    }

    @Transactional
    public OrderResponseDTO updateOrder(Long orderId, Long userId, OrderRequestDTO orderRequest) {
        log.info("Updating order with id: {} for user: {}", orderId, userId);

        Order existingOrder = orderRepository.findByIdAndUserIdAndDeletedFalse(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId + " for user: " + userId));

        if (orderRequest.getStatus() != null) {
            existingOrder.setStatus(orderRequest.getStatus());
        }
        if (orderRequest.getTotalPrice() != null) {
            existingOrder.setTotalPrice(orderRequest.getTotalPrice());
        }

        if (orderRequest.getOrderItems() != null) {
            existingOrder.getOrderItems().clear();
            List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                    .map(orderMapper::toEntity)
                    .peek(orderItem -> orderItem.setOrder(existingOrder))
                    .collect(Collectors.toList());
            existingOrder.setOrderItems(orderItems);
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with id: {}", orderId);

        return enrichOrderWithUserInfo(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Soft deleting order with id: {}", id);
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.softDelete();
        orderRepository.save(order);
        log.info("Order soft deleted successfully with id: {}", id);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        log.info("Soft deleting order with id: {} for user: {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserIdAndDeletedFalse(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId + " for user: " + userId));
        order.softDelete();
        orderRepository.save(order);
        log.info("Order soft deleted successfully with id: {}", orderId);
    }

    private OrderResponseDTO enrichOrderWithUserInfo(Order order) {
        OrderResponseDTO response = orderMapper.toResponse(order);
        try {
            // ВАЖНО: Общаемся с UserService для получения реальных данных пользователя
            UserInfoDTO userInfo = userInfoService.getUserById(order.getUserId());
            response.setUserInfo(userInfo);
        } catch (Exception e) {
            log.error("Failed to fetch user info for userId: {}", order.getUserId(), e);
            // Не бросаем исключение, а просто логируем ошибку
            // Response все равно вернется, но без userInfo или с дефолтными значениями из fallback
        }
        return response;
    }
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByUserWithFilter(Long userId, LocalDateTime startDate, LocalDateTime endDate,
                                                            List<String> statuses, Pageable pageable) {
        log.info("Fetching orders for user: {} with filters", userId);

        Specification<Order> spec = Specification.where(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.hasUserId(userId))
                .and(OrderSpecifications.createdAtBetween(startDate, endDate))
                .and(OrderSpecifications.hasStatusIn(statuses));

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(this::enrichOrderWithUserInfo);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrdersWithFilter(LocalDateTime startDate, LocalDateTime endDate,
                                                         List<String> statuses, Pageable pageable) {
        log.info("Fetching ALL orders with filters - startDate: {}, endDate: {}, statuses: {}",
                startDate, endDate, statuses);

        Specification<Order> spec = Specification.where(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.createdAtBetween(startDate, endDate))
                .and(OrderSpecifications.hasStatusIn(statuses));

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(this::enrichOrderWithUserInfo);
    }
}