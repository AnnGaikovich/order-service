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
}