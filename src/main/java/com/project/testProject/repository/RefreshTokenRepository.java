package com.project.testProject.repository;

import com.project.testProject.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Transactional
    void deleteByUserId(Long idx);
}
