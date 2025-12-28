//package org.example.orderservice.integration;
//
//import org.example.orderservice.dto.OrderItemRequestDTO;
//import org.example.orderservice.dto.OrderRequestDTO;
//import org.example.orderservice.dto.OrderResponseDTO;
//import org.example.orderservice.entity.Item;
//import org.example.orderservice.repository.ItemRepository;
//import org.example.orderservice.repository.OrderRepository;
//import org.example.orderservice.service.OrderService;
//import com.github.tomakehurst.wiremock.client.WireMock;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        properties = {
//                // This will override the user.service.url to use WireMock
//                "user.service.url=http://localhost:${wiremock.server.port}"
//        }
//)
//@Testcontainers
//@AutoConfigureWireMock(port = 0) // Random port
//class OrderServiceIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.6")
//            .withDatabaseName("order_service_db")
//            .withUsername("postgres")
//            .withPassword("123456");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
//        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
//        // Remove the user.service.url from here since we're setting it in @SpringBootTest properties
//    }
//
//    @Autowired
//    private OrderService orderService;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    @BeforeEach
//    void setUp() {
//        orderRepository.deleteAll();
//        itemRepository.deleteAll();
//        WireMock.reset();
//
//        // Setup default stub for user service
//        setupUserServiceStub();
//    }
//
//    private void setupUserServiceStub() {
//        // Default stub that will be used by all tests unless overridden
//        String userResponse = """
//            {
//                "id": 1,
//                "name": "Test",
//                "surname": "User",
//                "email": "test@user.com",
//                "paymentCards": [],
//                "birthDate": "1990-01-01",
//                "active": true,
//                "createdAt": "2023-01-01T00:00:00",
//                "updatedAt": "2023-01-01T00:00:00"
//            }
//            """;
//
//        stubFor(get(urlEqualTo("/api/v1/users/1"))
//                .withHeader("X-API-Key", equalTo("internal-auth-key-12345"))
//                .willReturn(aResponse()
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(userResponse)));
//    }
//
//    @Test
//    void createOrder_Integration_ShouldCreateOrderWithUserInfo() {
//        // Given
//        // Setup test item
//        Item item = new Item();
//        item.setName("Test Item");
//        item.setPrice(new BigDecimal("25.00"));
//        Item savedItem = itemRepository.save(item);
//
//        // Setup order request
//        OrderItemRequestDTO orderItem = new OrderItemRequestDTO(savedItem.getId(), 2);
//        OrderRequestDTO orderRequest = new OrderRequestDTO();
//        orderRequest.setOrderItems(Arrays.asList(orderItem));
//        orderRequest.setTotalPrice(new BigDecimal("50.00"));
//        orderRequest.setStatus("PENDING");
//
//        // Override the stub for this specific test
//        String userResponse = """
//            {
//                "id": 1,
//                "name": "Integration",
//                "surname": "Test",
//                "email": "integration@test.com",
//                "paymentCards": [],
//                "birthDate": "1990-01-01",
//                "active": true,
//                "createdAt": "2023-01-01T00:00:00",
//                "updatedAt": "2023-01-01T00:00:00"
//            }
//            """;
//
//        stubFor(get(urlEqualTo("/api/v1/users/1"))
//                .withHeader("X-API-Key", equalTo("internal-auth-key-12345"))
//                .willReturn(aResponse()
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(userResponse)));
//
//        // When
//        OrderResponseDTO result = orderService.createOrder(1L, orderRequest);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getUserId()).isEqualTo(1L);
//        assertThat(result.getUserInfo()).isNotNull();
//        assertThat(result.getUserInfo().getId()).isEqualTo(1L);
//        assertThat(result.getUserInfo().getName()).isEqualTo("Integration");
//        assertThat(result.getUserInfo().getEmail()).isEqualTo("integration@test.com");
//
//        // Verify WireMock was called
//        verify(getRequestedFor(urlEqualTo("/api/v1/users/1"))
//                .withHeader("X-API-Key", equalTo("internal-auth-key-12345")));
//    }
//
//    @Test
//    void createOrder_WhenUserServiceUnavailable_ShouldUseFallback() {
//        // Given
//        Item item = new Item();
//        item.setName("Test Item");
//        item.setPrice(new BigDecimal("25.00"));
//        Item savedItem = itemRepository.save(item);
//
//        OrderItemRequestDTO orderItem = new OrderItemRequestDTO(savedItem.getId(), 2);
//        OrderRequestDTO orderRequest = new OrderRequestDTO();
//        orderRequest.setOrderItems(Arrays.asList(orderItem));
//        orderRequest.setTotalPrice(new BigDecimal("50.00"));
//        orderRequest.setStatus("PENDING");
//
//        // Mock UserService failure - override the default stub
//        stubFor(get(urlEqualTo("/api/v1/users/1"))
//                .withHeader("X-API-Key", equalTo("internal-auth-key-12345"))
//                .willReturn(aResponse()
//                        .withStatus(500)
//                        .withFixedDelay(2000) // Add delay to ensure timeout/circuit breaker
//                        .withBody("Service Unavailable")));
//
//        // When
//        OrderResponseDTO result = orderService.createOrder(1L, orderRequest);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getUserId()).isEqualTo(1L);
//        // Should still return order even if user service is down
//        assertThat(result.getUserInfo()).isNotNull();
//        // Verify fallback user info is used (check your fallback implementation)
//        assertThat(result.getUserInfo().getName()).isEqualTo("Service"); // Or whatever your fallback returns
//    }
//
//    @Test
//    void getOrderById_Integration_ShouldReturnOrderWithUserInfo() {
//        // Given
//        // First create an order
//        Item item = new Item();
//        item.setName("Test Item");
//        item.setPrice(new BigDecimal("25.00"));
//        Item savedItem = itemRepository.save(item);
//
//        OrderItemRequestDTO orderItem = new OrderItemRequestDTO(savedItem.getId(), 2);
//        OrderRequestDTO orderRequest = new OrderRequestDTO();
//        orderRequest.setOrderItems(Arrays.asList(orderItem));
//        orderRequest.setTotalPrice(new BigDecimal("50.00"));
//        orderRequest.setStatus("PENDING");
//
//        // Override stub for this test
//        String userResponse = """
//            {
//                "id": 1,
//                "name": "Test",
//                "surname": "User",
//                "email": "test@user.com",
//                "paymentCards": [],
//                "birthDate": "1990-01-01",
//                "active": true,
//                "createdAt": "2023-01-01T00:00:00",
//                "updatedAt": "2023-01-01T00:00:00"
//            }
//            """;
//
//        stubFor(get(urlEqualTo("/api/v1/users/1"))
//                .withHeader("X-API-Key", equalTo("internal-auth-key-12345"))
//                .willReturn(aResponse()
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(userResponse)));
//
//        OrderResponseDTO createdOrder = orderService.createOrder(1L, orderRequest);
//
//        // When
//        OrderResponseDTO retrievedOrder = orderService.getOrderById(createdOrder.getId());
//
//        // Then
//        assertThat(retrievedOrder).isNotNull();
//        assertThat(retrievedOrder.getId()).isEqualTo(createdOrder.getId());
//        assertThat(retrievedOrder.getUserInfo()).isNotNull();
//        assertThat(retrievedOrder.getUserInfo().getName()).isEqualTo("Test");
//    }
//}