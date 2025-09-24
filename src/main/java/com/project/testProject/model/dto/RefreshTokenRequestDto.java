package com.project.testProject.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
