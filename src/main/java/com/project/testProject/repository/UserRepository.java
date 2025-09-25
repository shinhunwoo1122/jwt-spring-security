package com.project.testProject.repository;

import com.project.testProject.model.dto.UserIdProjection;
import com.project.testProject.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<UserIdProjection> findByUserId(String username);

    User findByUsername(String username);
}
