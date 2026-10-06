package com.green.spring_board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SignupRequest {

    @NotBlank
    @Email // 이메일 형식인지 검증
    @Size(max = 100)
    private String email; // 메일은 어떤 형식으로 규제해야 할까? @가 포함되어야 함.

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    @Size(max = 30)
    private String nickname;

}
