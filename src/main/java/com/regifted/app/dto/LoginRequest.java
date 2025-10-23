package com.regifted.app.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}
