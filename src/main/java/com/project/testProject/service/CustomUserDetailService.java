package com.project.testProject.service;

import com.project.testProject.model.entity.User;
import com.project.testProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(Long.valueOf(username));
        log.info("username = {}", username);
        log.info("user정보 조회하는 부분 체크 = {}", user);

        return new org.springframework.security.core.userdetails.User(
                username,
                user.get().getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.get().getRole()))
        );
    }
}
