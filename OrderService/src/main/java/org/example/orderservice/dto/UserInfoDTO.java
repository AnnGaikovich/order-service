package org.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoDTO {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private boolean active;
}