package com.project.testProject.security.jwt;


import com.project.testProject.security.jwt.filter.JwtAccessDeniedHandler;
import com.project.testProject.security.jwt.filter.JwtAuthenticationEntryPoint;
import com.project.testProject.security.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;



    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // AuthenticationConfiguration을 사용해 AuthenticationManager를 빈으로 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/test/**").permitAll()
                    .requestMatchers("/api/register/**").permitAll()
                    .requestMatchers("/api/login/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/path/admin").hasRole("ADMIN")
                    .requestMatchers("/api/path/manager").hasRole("MANAGER")
                    .requestMatchers("/api/path/user").hasRole("USER")
                    .anyRequest().authenticated()
            ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy(){
        //계층 정의 ROLE_ADMIN > ROLE_MANAGER > ROLE_USER 포함 형식
        String hierarchy = "ROLE_ADMIN > ROLE_MANAGER > ROLE_USER";
        return RoleHierarchyImpl.fromHierarchy(hierarchy);
    }

    @Bean
    public DefaultWebSecurityExpressionHandler webExpressionHandler(){
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }

}
