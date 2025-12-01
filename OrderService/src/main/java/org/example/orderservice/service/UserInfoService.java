package org.example.orderservice.service;

import org.example.orderservice.client.UserServiceClient;
import org.example.orderservice.dto.UserInfoDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserInfoDTO getUserById(Long userId) {
        log.info("Fetching user info from UserService for userId: {}", userId);
        UserInfoDTO userInfo = userServiceClient.getUserById(userId);
        log.info("Successfully fetched user info: {}", userInfo);
        return userInfo;
    }

    public UserInfoDTO getUserByIdFallback(Long userId, Exception ex) {
        log.warn("Circuit Breaker fallback triggered for getUserById({}), exception: {}", userId, ex.getMessage());
        UserInfoDTO defaultUser = createDefaultUser(userId);
        log.info("Returning default user: {}", defaultUser);
        return defaultUser;
    }

    private UserInfoDTO createDefaultUser(Long userId) {
        UserInfoDTO defaultUser = new UserInfoDTO();
        if (userId != null) {
            defaultUser.setId(userId);
        }
        defaultUser.setEmail("unavailable@example.com");
        defaultUser.setName("Service");
        defaultUser.setSurname("Unavailable");
        defaultUser.setActive(false);
        return defaultUser;
    }
}