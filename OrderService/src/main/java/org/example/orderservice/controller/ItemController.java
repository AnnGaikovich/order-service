package org.example.orderservice.controller;

import org.example.orderservice.dto.ItemRequestDTO;
import org.example.orderservice.dto.ItemResponseDTO;
import org.example.orderservice.service.ItemService;
import org.example.orderservice.util.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(
            @Valid @RequestBody ItemRequestDTO itemRequest,
            @RequestHeader("Authorization") String authorizationHeader) {

        List<String> roles = jwtTokenUtil.extractRolesFromToken(authorizationHeader);
        log.info("Received request to create item from user with roles: {}", roles);

        if (!roles.contains("ROLE_ADMIN")) {
            log.warn("Access denied: User does not have ADMIN role for creating item");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ItemResponseDTO itemResponse = itemService.createItem(itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems(
            @RequestHeader("Authorization") String authorizationHeader) {

        jwtTokenUtil.extractUserIdFromToken(authorizationHeader); // Проверка валидности токена
        log.info("Received request to get all items");

        List<ItemResponseDTO> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItem(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {

        jwtTokenUtil.extractUserIdFromToken(authorizationHeader); // Проверка валидности токена
        log.info("Received request to get item with id: {}", id);

        ItemResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }
}