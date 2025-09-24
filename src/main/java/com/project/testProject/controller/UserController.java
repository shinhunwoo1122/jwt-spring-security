package com.project.testProject.controller;

import com.project.testProject.common.*;
import com.project.testProject.model.dto.RefreshTokenRequestDto;
import com.project.testProject.model.dto.TokenResponseDto;
import com.project.testProject.model.dto.UserLoginDto;
import com.project.testProject.model.dto.UserRegistrationDto;
import com.project.testProject.model.entity.User;
import com.project.testProject.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> register(@Valid @RequestBody UserRegistrationDto userRegistrationDto){
        //회원 정보 받기
        User user = User.builder()
                .userId(userRegistrationDto.getUserId())
                .username(userRegistrationDto.getUsername())
                .password(userRegistrationDto.getPassword())
                .email(userRegistrationDto.getEmail())
                .build();
        //등록
        authService.registerUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).message("회원가입완료").build(), user));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse> login(@Valid @RequestBody UserLoginDto loginDto){
        ResultObject<Object> refreshToken = authService.login(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).message("로그인 완료").build(), refreshToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<BaseResponse> refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto){
        String refreshToken = requestDto.getRefreshToken();
        ResultObject<TokenResponseDto> tokenResponse = authService.refreshToken(refreshToken);
        if(!tokenResponse.isSuccess()){
            return ResponseEntity.ok(new ErrorResponse(MetaData.builder().result(false).message("인증실패했습니다").build()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).message("발급완료").build(), tokenResponse));
    }

    @GetMapping("/test")
    public ResponseEntity<BaseResponse> test(){
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(MetaData.builder().result(true).message("테스트완료").build(), null));
    }



}
