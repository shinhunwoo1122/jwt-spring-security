package com.project.testProject.service.impl;

import com.project.testProject.common.ResultObject;
import com.project.testProject.model.dto.TokenResponseDto;
import com.project.testProject.model.dto.UserLoginDto;
import com.project.testProject.model.entity.RefreshToken;
import com.project.testProject.model.entity.User;
import com.project.testProject.repository.RefreshTokenRepository;
import com.project.testProject.repository.UserRepository;
import com.project.testProject.security.jwt.JwtProvider;
import com.project.testProject.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Override
    public void registerUser(User user) {

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");

        log.info("user = {}", user);

        userRepository.save(user);
    }

    @Override
    public ResultObject<Object> login(UserLoginDto loginDto) {
        /* 1. 아이디 비밀번호로 인증 객체 생성 */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        log.info("1. 인증 토큰 생성: {}", authenticationToken);

        /* 2. AuthenticationManger를 통해 인증 실행 */
        log.info("2. AuthenticationManger 인증 시작");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info("2. 인증 성공: {}", authentication);

        /* 3. 인증이 성공하면 UserDetails 객체와 사용자 idx 가져옴 */
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("userDetails {}", userDetails);
        User user = userRepository.findByUsername(userDetails.getUsername());
        log.info("유저 객체 정보: {}", user);
        log.info("3. 사용자 정보 DB 조회 성공: idx={}, username={}", user.getIdx(), user.getUsername());

        /* 4. JwtProvider를 사용하여 토큰 생성 */
        String accessToken = jwtProvider.generateAccessToken(userDetails, user.getIdx());
        String refreshToken = jwtProvider.generateRefreshToken(userDetails, user.getIdx());
        log.info("4. 토큰 생성 완료 - 액세스 토큰: {}, 리프레시 토큰: {}", accessToken, refreshToken);

        /* 5. 기존 리프레시 토근이 있다면 삭제하고 새 토큰 저장 */
        log.info("5. 기존 리프레시 토큰 삭제 시작 (userIdx: {})", user.getIdx());
        refreshTokenRepository.deleteByUserId(user.getIdx());
        log.info("5. 기존 리프레시 토큰 삭제 완료");

        long refreshTokenExpiration = jwtProvider.getRefreshTokenExpiration();

        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setUserId(user.getIdx());
        token.setExpiryDate(LocalDateTime.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS));
        refreshTokenRepository.save(token);
        log.info("5. 새로운 리프레시 토큰 저장 완료");

        log.info("6. 로그인 프로세스 최종 완료. 응답 생성 시작");
        return ResultObject.builder().isSuccess(true).message("정상처리되었습니다.").data(
                TokenResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build()
        ).build();
    }

    @Override
    public ResultObject<TokenResponseDto> refreshToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResultObject.<TokenResponseDto>builder().message("유효하지 않은 리프레시 토큰입니다.").build();
        }

        // 2. 토큰에서 사용자 정보 추출
        Long userId = jwtProvider.getClaimFromToken(refreshToken).get("userIdx", Long.class);

        // 3. DB에서 리프레시 토큰 확인
        Optional<RefreshToken> storedTokenOpt = refreshTokenRepository.findById(userId);
        if (storedTokenOpt.isEmpty() || !storedTokenOpt.get().getToken().equals(refreshToken) || storedTokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResultObject.<TokenResponseDto>builder().message("리프레시 토큰이 만료되었거나 일치하지 않습니다.").build();
        }

        // 4. 사용자 정보 로드 및 새 액세스 토큰 생성
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole()))
        );
        String newAccessToken = jwtProvider.generateAccessToken(userDetails, user.getIdx());

        return ResultObject.<TokenResponseDto>builder().isSuccess(true).message("토큰이 갱신되었습니다.").data(
                TokenResponseDto.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .build()
        ).build();
    }
}
