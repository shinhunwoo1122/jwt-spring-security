package com.project.testProject.model;

import lombok.Data;

@Data
public class TokenPayload {
    private String subject;
    private Long userIdx;
    private String userId;
    private String role;
    private String ip;
}
