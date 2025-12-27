//package org.example.orderservice.service;
//
//import org.example.orderservice.client.UserServiceClient;
//import org.example.orderservice.dto.UserInfoDTO;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserInfoServiceUnitTest {
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @InjectMocks
//    private UserInfoService userInfoService;
//
//    private UserInfoDTO userInfoDTO;
//
//    @BeforeEach
//    void setUp() {
//        userInfoDTO = new UserInfoDTO();
//        userInfoDTO.setId(1L);
//        userInfoDTO.setName("Test User");
//        userInfoDTO.setEmail("test@example.com");
//        userInfoDTO.setActive(true);
//    }
//
//    @Test
//    void getUserById_ShouldReturnUserInfo() {
//        // Given
//        when(userServiceClient.getUserById(1L)).thenReturn(userInfoDTO);
//
//        // When
//        UserInfoDTO result = userInfoService.getUserById(1L);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getName()).isEqualTo("Test User");
//        verify(userServiceClient, times(1)).getUserById(1L);
//    }
//
//    @Test
//    void getUserById_WhenServiceFails_ShouldReturnFallbackUser() {
//        // Given
//        Exception testException = new RuntimeException("Service unavailable");
//
//        // When
//        UserInfoDTO result = userInfoService.getUserByIdFallback(1L, testException);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getEmail()).isEqualTo("unavailable@example.com");
//        assertThat(result.getName()).isEqualTo("Service");
//        assertThat(result.getSurname()).isEqualTo("Unavailable");
//        assertThat(result.isActive()).isFalse();
//    }
//
//    @Test
//    void getUserById_WithNullUserId_ShouldReturnFallbackUser() {
//        // Given
//        Exception testException = new RuntimeException("Invalid user ID");
//
//        // When
//        UserInfoDTO result = userInfoService.getUserByIdFallback(null, testException);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isNull();
//        assertThat(result.getEmail()).isEqualTo("unavailable@example.com");
//        assertThat(result.getName()).isEqualTo("Service");
//        assertThat(result.getSurname()).isEqualTo("Unavailable");
//        assertThat(result.isActive()).isFalse();
//    }
//
//    // Дополнительный тест для проверки fallback метода напрямую
//    @Test
//    void getUserByIdFallback_ShouldReturnDefaultUser() {
//        // Given
//        Exception testException = new RuntimeException("Test exception");
//
//        // When
//        UserInfoDTO result = userInfoService.getUserByIdFallback(1L, testException);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getEmail()).isEqualTo("unavailable@example.com");
//        assertThat(result.getName()).isEqualTo("Service");
//        assertThat(result.getSurname()).isEqualTo("Unavailable");
//        assertThat(result.isActive()).isFalse();
//    }
//}