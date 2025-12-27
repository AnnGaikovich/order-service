package org.example.orderservice.controller;

import org.example.orderservice.dto.ItemRequestDTO;
import org.example.orderservice.dto.ItemResponseDTO;
import org.example.orderservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponseDTO> createItem(
            @Valid @RequestBody ItemRequestDTO itemRequest) {

        log.info("Received request to create item");
        ItemResponseDTO itemResponse = itemService.createItem(itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        log.info("Received request to get all items");
        List<ItemResponseDTO> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ItemResponseDTO> getItem(@PathVariable Long id) {
        log.info("Received request to get item with id: {}", id);
        ItemResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponseDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequestDTO itemRequest) {

        log.info("Received request to update item with id: {}", id);
        ItemResponseDTO updatedItem = itemService.updateItem(id, itemRequest);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        log.info("Received request to delete item with id: {}", id);
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}