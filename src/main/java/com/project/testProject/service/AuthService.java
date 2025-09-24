package com.project.testProject.service;

import com.project.testProject.common.ResultObject;
import com.project.testProject.model.dto.TokenResponseDto;
import com.project.testProject.model.dto.UserLoginDto;
import com.project.testProject.model.entity.User;
import jakarta.validation.Valid;

public interface AuthService {
    void registerUser(User user);

    ResultObject<Object> login(@Valid UserLoginDto loginDto);

    ResultObject<TokenResponseDto> refreshToken(String refreshToken);
}
