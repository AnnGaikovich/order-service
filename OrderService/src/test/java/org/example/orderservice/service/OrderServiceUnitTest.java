//package org.example.orderservice.service;
//
//import org.example.orderservice.dto.OrderItemRequestDTO;
//import org.example.orderservice.dto.OrderRequestDTO;
//import org.example.orderservice.dto.OrderResponseDTO;
//import org.example.orderservice.dto.UserInfoDTO;
//import org.example.orderservice.entity.Order;
//import org.example.orderservice.entity.OrderItem;
//import org.example.orderservice.mapper.OrderMapper;
//import org.example.orderservice.repository.ItemRepository;
//import org.example.orderservice.repository.OrderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceUnitTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private OrderMapper orderMapper;
//
//    @Mock
//    private UserInfoService userInfoService;
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @InjectMocks
//    private OrderService orderService;
//
//    private OrderRequestDTO orderRequestDTO;
//    private Order order;
//    private OrderResponseDTO orderResponseDTO;
//    private UserInfoDTO userInfoDTO;
//
//    @BeforeEach
//    void setUp() {
//        // Setup test data
//        OrderItemRequestDTO itemRequest = new OrderItemRequestDTO(1L, 2);
//        orderRequestDTO = new OrderRequestDTO();
//        orderRequestDTO.setOrderItems(new ArrayList<>(Arrays.asList(itemRequest)));
//        orderRequestDTO.setTotalPrice(new BigDecimal("100.00"));
//        orderRequestDTO.setStatus("PENDING");
//
//        order = new Order();
//        order.setId(1L);
//        order.setUserId(1L);
//
//        // СОЗДАЕМ НОРМАЛЬНЫЙ OrderItem БЕЗ NULL
//        OrderItem orderItem = new OrderItem();
//        orderItem.setId(1L); // ДОБАВЬТЕ ЭТО!
//        orderItem.setItemId(1L);
//        orderItem.setQuantity(2);
//        orderItem.setOrder(order); // ВАЖНО: установите связь!
//
//        order.setOrderItems(new ArrayList<>(Arrays.asList(orderItem))); // Используем ArrayList
//
//        orderResponseDTO = new OrderResponseDTO();
//        orderResponseDTO.setId(1L);
//        orderResponseDTO.setUserId(1L);
//
//        userInfoDTO = new UserInfoDTO();
//        userInfoDTO.setId(1L);
//        userInfoDTO.setName("Test User");
//        userInfoDTO.setEmail("test@example.com");
//    }
//
//    @Test
//    void createOrder_ShouldCreateOrderSuccessfully() {
//        // Given
//        OrderItem mockOrderItem = new OrderItem();
//        mockOrderItem.setItemId(1L);
//        mockOrderItem.setQuantity(2);
//
//        when(itemRepository.existsById(anyLong())).thenReturn(true);
//        when(orderMapper.toEntity(any(OrderRequestDTO.class))).thenReturn(order);
//        when(orderRepository.save(any(Order.class))).thenReturn(order);
//        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);
//        when(userInfoService.getUserById(anyLong())).thenReturn(userInfoDTO);
//
//        // ДОБАВЛЯЕМ МОКИРОВАНИЕ
//        when(orderMapper.toEntity(any(OrderItemRequestDTO.class))).thenReturn(mockOrderItem);
//
//        // When
//        OrderResponseDTO result = orderService.createOrder(1L, orderRequestDTO);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(orderRepository, times(1)).save(any(Order.class));
//        verify(itemRepository, times(1)).existsById(1L);
//        verify(orderMapper, atLeastOnce()).toEntity(any(OrderItemRequestDTO.class));
//    }
//
//    @Test
//    void createOrder_WithNonExistentItem_ShouldThrowException() {
//        // Given
//        when(itemRepository.existsById(anyLong())).thenReturn(false);
//
//        // When & Then
//        assertThatThrownBy(() -> orderService.createOrder(1L, orderRequestDTO))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Item not found");
//
//        verify(orderRepository, never()).save(any(Order.class));
//    }
//
//    @Test
//    void getOrderById_ShouldReturnOrder() {
//        // Given
//        when(orderRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(order));
//        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);
//        when(userInfoService.getUserById(anyLong())).thenReturn(userInfoDTO);
//
//        // When
//        OrderResponseDTO result = orderService.getOrderById(1L);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(orderRepository, times(1)).findByIdAndDeletedFalse(1L);
//    }
//
//    @Test
//    void getOrderById_WithNonExistentOrder_ShouldThrowException() {
//        // Given
//        when(orderRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());
//
//        // When & Then
//        assertThatThrownBy(() -> orderService.getOrderById(1L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Order not found");
//    }
//
//    @Test
//    void getOrdersByUserId_ShouldReturnUserOrders() {
//        // Given
//        List<Order> orders = Arrays.asList(order);
//        when(orderRepository.findByUserIdAndDeletedFalse(anyLong())).thenReturn(orders);
//        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);
//        when(userInfoService.getUserById(anyLong())).thenReturn(userInfoDTO);
//
//        // When
//        List<OrderResponseDTO> result = orderService.getOrdersByUserId(1L);
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getId()).isEqualTo(1L);
//        verify(orderRepository, times(1)).findByUserIdAndDeletedFalse(1L);
//    }
//
//    @Test
//    void updateOrder_ShouldUpdateOrderSuccessfully() {
//        // Given
//        // Создаем реальный OrderItem для мока
//        OrderItem mockOrderItem = new OrderItem();
//        mockOrderItem.setItemId(1L);
//        mockOrderItem.setQuantity(2);
//
//        when(orderRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(order));
//        when(orderRepository.save(any(Order.class))).thenReturn(order);
//        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);
//        when(userInfoService.getUserById(anyLong())).thenReturn(userInfoDTO);
//
//        // ДОБАВЛЯЕМ МОКИРОВАНИЕ ДЛЯ OrderItemRequestDTO -> OrderItem
//        when(orderMapper.toEntity(any(OrderItemRequestDTO.class))).thenReturn(mockOrderItem);
//
//        // When
//        OrderResponseDTO result = orderService.updateOrder(1L, orderRequestDTO);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(orderRepository, times(1)).findByIdAndDeletedFalse(1L);
//        verify(orderRepository, times(1)).save(any(Order.class));
//        verify(userInfoService, times(1)).getUserById(1L);
//
//        // Проверяем, что мок был вызван для преобразования OrderItem
//        verify(orderMapper, atLeastOnce()).toEntity(any(OrderItemRequestDTO.class));
//    }
//
////    @Test
////    void deleteOrder_ShouldSoftDeleteOrder() {
////        // Given
////        when(orderRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(order));
////        when(orderRepository.save(any(Order.class))).thenReturn(order);
////
////        // When
////        orderService.deleteOrder(1L);
////
////        // Then
////        verify(orderRepository, times(1)).save(order);
////        assertThat(order.isDeleted()).isTrue();
////    }
//
//    @Test
//    void getAllOrdersWithFilter_ShouldReturnFilteredOrders() {
//        // Given
//        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order));
//        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
//        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);
//        when(userInfoService.getUserById(anyLong())).thenReturn(userInfoDTO);
//
//        Pageable pageable = PageRequest.of(0, 10);
//        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
//        LocalDateTime endDate = LocalDateTime.now();
//        List<String> statuses = Arrays.asList("PENDING", "COMPLETED");
//
//        // When
//        Page<OrderResponseDTO> result = orderService.getAllOrdersWithFilter(
//                startDate, endDate, statuses, pageable);
//
//        // Then
//        assertThat(result).hasSize(1);
//        verify(orderRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
//    }
//
//    @Test
//    void enrichOrderWithUserInfo_WhenUserServiceFails_ShouldReturnOrderWithoutUserInfo() {
//        // Given
//        when(orderRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(order));
//        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponseDTO);
//        when(userInfoService.getUserById(anyLong())).thenThrow(new RuntimeException("User service unavailable"));
//
//        // When
//        OrderResponseDTO result = orderService.getOrderById(1L);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        // Verify that user service was called but the order was still returned
//        verify(userInfoService, times(1)).getUserById(1L);
//    }
//}