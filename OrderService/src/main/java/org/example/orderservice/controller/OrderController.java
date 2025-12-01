package org.example.orderservice.controller;

import org.example.orderservice.dto.OrderRequestDTO;
import org.example.orderservice.dto.OrderResponseDTO;
import org.example.orderservice.exception.ErrorResponse;
import org.example.orderservice.service.OrderService;
import org.example.orderservice.util.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequest,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            Long userId = jwtTokenUtil.extractUserIdFromToken(authorizationHeader);
            List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);

            log.info("Received request to create order for user: {} with roles: {}", userId, roles);

            OrderResponseDTO orderResponse = orderService.createOrder(userId, orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Invalid or expired token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), LocalDateTime.now()));
            }
            throw ex;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = jwtTokenUtil.extractUserIdFromToken(authorizationHeader);
        List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);

        log.info("Received request to get order with id: {} from user: {} with roles: {}", id, userId, roles);

        OrderResponseDTO orderResponse;
        if (roles.contains("ROLE_ADMIN")) {
            orderResponse = orderService.getOrderById(id);
        } else {
            orderResponse = orderService.getOrderByIdAndUser(id, userId);
        }

        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Long userId = jwtTokenUtil.extractUserIdFromToken(authorizationHeader);
        List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);

        log.info("Received request to get orders from user: {} with roles: {}", userId, roles);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponseDTO> orders;
        if (roles.contains("ROLE_ADMIN")) {
            orders = orderService.getAllOrdersWithFilter(startDate, endDate, statuses, pageable);
        } else {
            orders = orderService.getOrdersByUserWithFilter(userId, startDate, endDate, statuses, pageable);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{targetUserId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(
            @PathVariable Long targetUserId,
            @RequestHeader("Authorization") String authorizationHeader) {

        Long authenticatedUserId = jwtTokenUtil.extractUserIdFromToken(authorizationHeader);
        List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);

        log.info("Received request to get orders for user: {} from user: {} with roles: {}",
                targetUserId, authenticatedUserId, roles);

        if (!roles.contains("ROLE_ADMIN") && !authenticatedUserId.equals(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(targetUserId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDTO orderRequest,
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = jwtTokenUtil.extractUserIdFromToken(authorizationHeader);
        List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);

        log.info("Received request to update order with id: {} from user: {} with roles: {}", id, userId, roles);

        OrderResponseDTO orderResponse;
        if (roles.contains("ROLE_ADMIN")) {
            orderResponse = orderService.updateOrder(id, orderRequest);
        } else {
            orderResponse = orderService.updateOrder(id, userId, orderRequest);
        }

        return ResponseEntity.ok(orderResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = jwtTokenUtil.extractUserIdFromToken(authorizationHeader);
        List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);

        log.info("Received request to delete order with id: {} from user: {} with roles: {}", id, userId, roles);

        if (roles.contains("ROLE_ADMIN")) {
            orderService.deleteOrder(id);
        } else {
            orderService.deleteOrder(id, userId);
        }

        return ResponseEntity.noContent().build();
    }
}