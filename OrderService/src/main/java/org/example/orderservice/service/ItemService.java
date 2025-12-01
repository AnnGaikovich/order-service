package org.example.orderservice.service;

import org.example.orderservice.dto.ItemRequestDTO;
import org.example.orderservice.dto.ItemResponseDTO;
import org.example.orderservice.entity.Item;
import org.example.orderservice.mapper.ItemMapper;
import org.example.orderservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemResponseDTO createItem(ItemRequestDTO itemRequest) {
        log.info("Creating new item: {}", itemRequest.getName());

        Item item = itemMapper.toEntity(itemRequest);
        Item savedItem = itemRepository.save(item);

        log.info("Item created successfully with id: {}", savedItem.getId());
        return itemMapper.toResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDTO> getAllItems() {
        log.info("Fetching all items");
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponseDTO getItemById(Long id) {
        log.info("Fetching item with id: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
        return itemMapper.toResponse(item);
    }

    @Transactional
    public ItemResponseDTO updateItem(Long id, ItemRequestDTO itemRequest) {
        log.info("Updating item with id: {}", id);

        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        if (itemRequest.getName() != null) {
            existingItem.setName(itemRequest.getName());
        }
        if (itemRequest.getPrice() != null) {
            existingItem.setPrice(itemRequest.getPrice());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Item updated successfully with id: {}", id);

        return itemMapper.toResponse(updatedItem);
    }

    @Transactional
    public void deleteItem(Long id) {
        log.info("Deleting item with id: {}", id);

        if (!itemRepository.existsById(id)) {
            throw new RuntimeException("Item not found with id: " + id);
        }

        itemRepository.deleteById(id);
        log.info("Item deleted successfully with id: {}", id);
    }
}