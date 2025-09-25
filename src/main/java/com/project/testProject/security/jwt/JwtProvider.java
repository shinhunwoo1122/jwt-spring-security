package com.project.testProject.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.testProject.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Getter
public class JwtProvider {

    private final Key signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String tokenPrefix;
    private final String header;
    private final ObjectMapper objectMapper;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.expiration}") long accessTokenExpiration,
                       @Value("${jwt.refresh-expiration}") long refreshTokenExpiration,
                       @Value("${jwt.token-prefix}") String tokenPrefix,
                       @Value("${jwt.header}") String header){

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes); // base64 디코딩한 키를 사용
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.header = header;
        this.tokenPrefix = tokenPrefix;
        this.objectMapper = new ObjectMapper();
    }

    /* accessToken 생성 */
    public String generateAccessToken(UserDetails userDetails, User user){
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("userIdx", user.getId())
                .claim("userId", user.getUserId())
                .claim("userName", user.getUsername())
                .claim("role", authorities)
                .claim("ip", getClientIp())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /* refreshToken 생성 */
    public String generateRefreshToken(UserDetails userDetails, User user){
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("userIdx", user.getId())
                .claim("userId", user.getUserId())
                .claim("userName", user.getUsername())
                .claim("role", authorities)
                .claim("ip", getClientIp())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /* 토큰에서 사용자 payload (claims) body내용 map으로 추출 */
    public Claims getClaimFromToken(String token){

        // Bearer 접두사 제거
        if (token.startsWith(tokenPrefix)) { // tokenPrefix == "Bearer "
            token = token.substring(tokenPrefix.length()).trim();
        }

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token) // <-- 여기!
                .getBody();
    }

    /* 토큰에서 원하는 클래스 타입으로 payload body 내용 추출하는 메소드 */
    public<T> T getPayloadFormToken(String token, Class<T> vo){
        Claims claims = getClaimFromToken(token);
        return objectMapper.convertValue(claims, vo);
    }

    /* 토큰 유효성 검증 */
    public boolean validateToken(String token){
        try {
            if (token.startsWith(tokenPrefix)) {
                token = token.substring(tokenPrefix.length()).trim();
            }
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            // io.jsonwebtoken.security.SecurityException 또는 MalformedJwtException: 토큰의 서명이 위조되거나 형식이 잘못된 경우 발생합니다.
            return true;
        }catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e){
            log.info("잘못된 JWT 서명입니다. {}", e.getMessage());
        }catch (ExpiredJwtException e){ //토큰의 만료 시간이 지난 경우 발생합니다.
            log.info("만료된 JWT 토큰입니다. {}", e.getMessage());
        }catch (UnsupportedJwtException e){ //지원되지 않는 형식의 토큰인 경우 발생합니다.
            log.info("지원되지 않는 JWT 토큰입니다. {}", e.getMessage());
        }catch (IllegalArgumentException e){ //토큰 문자열이 비어있거나 null인 경우 발생합니다.
            log.info("JWT 토큰이 잘못 되었습니다. {}", e.getMessage());
        }
        return false;
    }

    /* ServletRequestAttributes 구현체를 통해 request취득 */
    private String getClientIp(){
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(attributes -> (ServletRequestAttributes)attributes)
                .map(ServletRequestAttributes::getRequest)
                .map(this::extractIpFromRequest)
                .orElse("unknown");
    }

    /* ip취득 프록시인경우 x-Forwarded-For을 통해 0번째 ip취득 or 없는 경우 request.getRemoteAddr();를 통해 ip취득 */
    private String extractIpFromRequest(HttpServletRequest request){
        //x-Forwarded-For 헤더 확인 있으면 첫번째 IP 없으면 getRemoteAddr()로 IP 취득
        return Optional.ofNullable(request.getHeader("x-Forwarded-For"))
                .filter(header -> !header.isEmpty())
                .map(header -> header.split(",")[0].trim())
                .orElseGet(request::getRemoteAddr);
    }
}
