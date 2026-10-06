package com.green.spring_board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class LoginRequest {
    @NotBlank //* null 또는 빈 문자열 방지
    private String email;
    @NotBlank
    private String password;
}
