package org.example.orderservice.service;

import org.example.orderservice.dto.ItemRequestDTO;
import org.example.orderservice.dto.ItemResponseDTO;
import org.example.orderservice.entity.Item;
import org.example.orderservice.mapper.ItemMapper;
import org.example.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private ItemRequestDTO itemRequestDTO;
    private Item item;
    private ItemResponseDTO itemResponseDTO;

    @BeforeEach
    void setUp() {
        itemRequestDTO = new ItemRequestDTO();
        itemRequestDTO.setName("Test Item");
        itemRequestDTO.setPrice(new BigDecimal("29.99"));

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(new BigDecimal("29.99"));

        itemResponseDTO = new ItemResponseDTO();
        itemResponseDTO.setId(1L);
        itemResponseDTO.setName("Test Item");
        itemResponseDTO.setPrice(new BigDecimal("29.99"));
    }

    @Test
    void createItem_ShouldCreateItemSuccessfully() {
        // Given
        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toResponse(any(Item.class))).thenReturn(itemResponseDTO);

        // When
        ItemResponseDTO result = itemService.createItem(itemRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Item");
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void getItemById_ShouldReturnItem() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toResponse(any(Item.class))).thenReturn(itemResponseDTO);

        // When
        ItemResponseDTO result = itemService.getItemById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void getItemById_WithNonExistentItem_ShouldThrowException() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> itemService.getItemById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void getAllItems_ShouldReturnAllItems() {
        // Given
        List<Item> items = Arrays.asList(item);
        when(itemRepository.findAll()).thenReturn(items);
        when(itemMapper.toResponse(any(Item.class))).thenReturn(itemResponseDTO);

        // When
        List<ItemResponseDTO> result = itemService.getAllItems();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void updateItem_ShouldUpdateItemSuccessfully() {
        // Given
        ItemRequestDTO updateRequest = new ItemRequestDTO();
        updateRequest.setName("Updated Item");
        updateRequest.setPrice(new BigDecimal("39.99"));

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Item");
        updatedItem.setPrice(new BigDecimal("39.99"));

        ItemResponseDTO updatedResponse = new ItemResponseDTO();
        updatedResponse.setId(1L);
        updatedResponse.setName("Updated Item");
        updatedResponse.setPrice(new BigDecimal("39.99"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toResponse(any(Item.class))).thenReturn(updatedResponse);

        // When
        ItemResponseDTO result = itemService.updateItem(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Item");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("39.99"));
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void deleteItem_ShouldDeleteItemSuccessfully() {
        // Given
        when(itemRepository.existsById(1L)).thenReturn(true);

        // When
        itemService.deleteItem(1L);

        // Then
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteItem_WithNonExistentItem_ShouldThrowException() {
        // Given
        when(itemRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> itemService.deleteItem(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");

        verify(itemRepository, never()).deleteById(1L);
    }
}