package org.example.orderservice.service;

import org.example.orderservice.dto.*;
import org.example.orderservice.entity.Item;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderItem;
import org.example.orderservice.enums.OrderStatus;
import org.example.orderservice.exception.*;
import org.example.orderservice.mapper.OrderItemMapper;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserInfoService userInfoService;
    private final ItemRepository itemRepository;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderResponseDTO createOrder(Long userId, OrderRequestDTO orderRequest) {
        log.info("Creating order for user from token: {}", userId);

        List<Long> itemIds = orderRequest.getOrderItems().stream()
                .map(OrderItemRequestDTO::getItemId)
                .collect(Collectors.toList());

        Map<Long, Item> items = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        for (OrderItemRequestDTO itemRequest : orderRequest.getOrderItems()) {
            if (!items.containsKey(itemRequest.getItemId())) {
                throw new ResourceNotFoundException("Item not found with id: " + itemRequest.getItemId());
            }
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setDeleted(false);

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemRequest : orderRequest.getOrderItems()) {
            Item item = items.get(itemRequest.getItemId());
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(item.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setOrder(order);

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            totalPrice = totalPrice.add(itemTotal);

            order.getOrderItems().add(orderItem);
        }

        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {}", savedOrder.getId());

        return enrichOrderWithUserInfo(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return enrichOrderWithUserInfo(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByIdAndUser(Long orderId, Long userId) {
        log.info("Fetching order with id: {} for user: {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserIdAndDeletedFalse(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId + " for user: " + userId));
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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        updateOrderFromRequest(existingOrder, orderRequest);

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with id: {}", id);

        return enrichOrderWithUserInfo(updatedOrder);
    }

    @Transactional
    public OrderResponseDTO updateOrder(Long orderId, Long userId, OrderRequestDTO orderRequest) {
        log.info("Updating order with id: {} for user: {}", orderId, userId);

        Order existingOrder = orderRepository.findByIdAndUserIdAndDeletedFalse(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId + " for user: " + userId));

        updateOrderFromRequest(existingOrder, orderRequest);

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with id: {}", orderId);

        return enrichOrderWithUserInfo(updatedOrder);
    }

    private void updateOrderFromRequest(Order order, OrderRequestDTO orderRequest) {
        if (orderRequest.getStatus() != null) {
            try {
                OrderStatus status = OrderStatus.valueOf(orderRequest.getStatus());
                order.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid order status: " + orderRequest.getStatus());
            }
        }

        if (orderRequest.getOrderItems() != null && !orderRequest.getOrderItems().isEmpty()) {

            List<Long> itemIds = orderRequest.getOrderItems().stream()
                    .map(OrderItemRequestDTO::getItemId)
                    .collect(Collectors.toList());

            Map<Long, Item> items = itemRepository.findAllById(itemIds).stream()
                    .collect(Collectors.toMap(Item::getId, Function.identity()));

            for (OrderItemRequestDTO itemRequest : orderRequest.getOrderItems()) {
                if (!items.containsKey(itemRequest.getItemId())) {
                    throw new ResourceNotFoundException("Item not found with id: " + itemRequest.getItemId());
                }
            }

            order.getOrderItems().clear();

            BigDecimal totalPrice = BigDecimal.ZERO;

            for (OrderItemRequestDTO itemRequest : orderRequest.getOrderItems()) {
                Item item = items.get(itemRequest.getItemId());
                OrderItem orderItem = new OrderItem();
                orderItem.setItemId(item.getId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setOrder(order);

                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);

                order.getOrderItems().add(orderItem);
            }

            order.setTotalPrice(totalPrice);
        }
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Soft deleting order with id: {}", id);
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.softDelete();
        orderRepository.save(order);
        log.info("Order soft deleted successfully with id: {}", id);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        log.info("Soft deleting order with id: {} for user: {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserIdAndDeletedFalse(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId + " for user: " + userId));
        order.softDelete();
        orderRepository.save(order);
        log.info("Order soft deleted successfully with id: {}", orderId);
    }

    private OrderResponseDTO enrichOrderWithUserInfo(Order order) {
        OrderResponseDTO response = orderMapper.toResponse(order);

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponseDTO> enrichedItems = enrichOrderItems(order);
            response.setOrderItems(enrichedItems);
        }

        UserInfoDTO userInfo = userInfoService.getUserById(order.getUserId());
        response.setUserInfo(userInfo);
        return response;
    }

    private List<OrderItemResponseDTO> enrichOrderItems(Order order) {

        List<Long> itemIds = order.getOrderItems().stream()
                .map(OrderItem::getItemId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Item> itemsMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        return order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderItemResponseDTO dto = orderItemMapper.toResponse(orderItem);

                    Item item = itemsMap.get(orderItem.getItemId());
                    if (item != null) {
                        dto.setItemName(item.getName());
                        dto.setItemPrice(item.getPrice());
                        dto.setSubtotal(item.getPrice().multiply(
                                BigDecimal.valueOf(orderItem.getQuantity())
                        ));
                    } else {
                        log.warn("Item with id {} not found for order {}",
                                orderItem.getItemId(), order.getId());
                        dto.setItemName("Unknown Item");
                        dto.setItemPrice(BigDecimal.ZERO);
                        dto.setSubtotal(BigDecimal.ZERO);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByUserWithFilter(Long userId, LocalDateTime startDate, LocalDateTime endDate,
                                                            List<String> statuses, Pageable pageable) {
        log.info("Fetching orders for user: {} with filters", userId);

        List<OrderStatus> orderStatuses = null;
        if (statuses != null && !statuses.isEmpty()) {
            orderStatuses = statuses.stream()
                    .map(status -> {
                        try {
                            return OrderStatus.valueOf(status);
                        } catch (IllegalArgumentException e) {
                            throw new ValidationException("Invalid order status in filter: " + status);
                        }
                    })
                    .collect(Collectors.toList());
        }

        Specification<Order> spec = Specification.where(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.hasUserId(userId))
                .and(OrderSpecifications.createdAtBetween(startDate, endDate))
                .and(OrderSpecifications.hasStatusIn(orderStatuses));

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(this::enrichOrderWithUserInfo);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrdersWithFilter(LocalDateTime startDate, LocalDateTime endDate,
                                                         List<String> statuses, Pageable pageable) {
        log.info("Fetching ALL orders with filters - startDate: {}, endDate: {}, statuses: {}",
                startDate, endDate, statuses);

        List<OrderStatus> orderStatuses = null;
        if (statuses != null && !statuses.isEmpty()) {
            orderStatuses = statuses.stream()
                    .map(status -> {
                        try {
                            return OrderStatus.valueOf(status);
                        } catch (IllegalArgumentException e) {
                            throw new ValidationException("Invalid order status in filter: " + status);
                        }
                    })
                    .collect(Collectors.toList());
        }

        Specification<Order> spec = Specification.where(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.createdAtBetween(startDate, endDate))
                .and(OrderSpecifications.hasStatusIn(orderStatuses));

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(this::enrichOrderWithUserInfo);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }
}