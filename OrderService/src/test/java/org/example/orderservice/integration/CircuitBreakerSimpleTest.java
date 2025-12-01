package org.example.orderservice.integration;

import org.example.orderservice.dto.OrderItemRequestDTO;
import org.example.orderservice.dto.OrderRequestDTO;
import org.example.orderservice.dto.OrderResponseDTO;
import org.example.orderservice.entity.Item;
import org.example.orderservice.repository.ItemRepository;
import org.example.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CircuitBreakerSimpleTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.6")
            .withDatabaseName("order_service_db")
            .withUsername("postgres")
            .withPassword("123456");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("user.service.url", () -> "http://nonexistent-service:9999");
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
    }

    @Test
    void whenUserServiceUnavailable_ShouldUseCircuitBreakerFallback() {
        // Given
        Item item = new Item();
        item.setName("Test Item");
        item.setPrice(new BigDecimal("25.00"));
        Item savedItem = itemRepository.save(item);

        OrderItemRequestDTO orderItem = new OrderItemRequestDTO(savedItem.getId(), 2);
        OrderRequestDTO orderRequest = new OrderRequestDTO();
        orderRequest.setOrderItems(Arrays.asList(orderItem));
        orderRequest.setTotalPrice(new BigDecimal("50.00"));
        orderRequest.setStatus("PENDING");

        // When
        OrderResponseDTO result = orderService.createOrder(1L, orderRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getEmail()).isEqualTo("unavailable@example.com");
        assertThat(result.getUserInfo().getName()).isEqualTo("Service");
        assertThat(result.getUserInfo().getSurname()).isEqualTo("Unavailable");
        assertThat(result.getUserInfo().isActive()).isFalse();
    }
}