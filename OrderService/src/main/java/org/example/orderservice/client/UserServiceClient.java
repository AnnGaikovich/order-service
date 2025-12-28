package org.example.orderservice.client;

import org.example.orderservice.config.FeignConfig;
import org.example.orderservice.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService", url = "${user.service.url:http://localhost:8080}", configuration = FeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    UserInfoDTO getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/users/email/{email}")
    UserInfoDTO getUserByEmail(@PathVariable("email") String email);
}