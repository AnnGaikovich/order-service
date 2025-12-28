package org.example.orderservice.controller;

import org.example.orderservice.dto.OrderRequestDTO;
import org.example.orderservice.dto.OrderResponseDTO;
import org.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.auth.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequest) {

        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Received request to create order for user: {}", userId);

        OrderResponseDTO orderResponse = orderService.createOrder(userId, orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {

        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Received request to get order with id: {} from user: {}", id, userId);

        OrderResponseDTO orderResponse;
        if (SecurityUtils.isAdmin()) {
            orderResponse = orderService.getOrderById(id);
        } else {
            orderResponse = orderService.getOrderByIdAndUser(id, userId);
        }

        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<OrderResponseDTO>> getOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Received request to get orders from user: {}", userId);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponseDTO> orders;
        if (SecurityUtils.isAdmin()) {
            orders = orderService.getAllOrdersWithFilter(startDate, endDate, statuses, pageable);
        } else {
            orders = orderService.getOrdersByUserWithFilter(userId, startDate, endDate, statuses, pageable);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{targetUserId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(
            @PathVariable Long targetUserId) {

        Long authenticatedUserId = SecurityUtils.getCurrentUserId();
        log.info("Received request to get orders for user: {} from user: {}",
                targetUserId, authenticatedUserId);

        if (!SecurityUtils.isAdmin() && !authenticatedUserId.equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(targetUserId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDTO orderRequest) {

        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Received request to update order with id: {} from user: {}", id, userId);

        OrderResponseDTO orderResponse;
        if (SecurityUtils.isAdmin()) {
            orderResponse = orderService.updateOrder(id, orderRequest);
        } else {
            orderResponse = orderService.updateOrder(id, userId, orderRequest);
        }

        return ResponseEntity.ok(orderResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {

        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Received request to delete order with id: {} from user: {}", id, userId);

        if (SecurityUtils.isAdmin()) {
            orderService.deleteOrder(id);
        } else {
            orderService.deleteOrder(id, userId);
        }

        return ResponseEntity.noContent().build();
    }
}