package com.project.testProject.security.jwt.filter;


import com.project.testProject.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean skip = path.startsWith("/api/test") || path.startsWith("/api/register") || path.startsWith("/api/login") || path.startsWith("/api/auth");
        log.info("shouldNotFilter {}? {}", path, skip);
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        log.info("Authorization 헤더 토큰: {}", token); // 토큰이 실제로 들어오는지 확인

        if(token != null && jwtProvider.validateToken(token)){
            //토근에서 페이로드(클레임)을 추출 사용자 정보와 권한 직접 가져옴.
            Claims claims = jwtProvider.getClaimFromToken(token);
            String userId = claims.getSubject();
            log.info("userId={}", userId);
            String roles = claims.get("role", String.class);

            //권한 문자열을 SimpleGrantedAuthority 객체 컬렉션으로 변환
            Collection<? extends GrantedAuthority> authorities = List.of();
            if (roles != null && !roles.isBlank()) {
                authorities = Arrays.stream(roles.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request){
        String bearerToken  = request.getHeader(jwtProvider.getHeader());
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProvider.getTokenPrefix())){
            return bearerToken.substring(jwtProvider.getTokenPrefix().length());
        }
        return null;
    }
}
