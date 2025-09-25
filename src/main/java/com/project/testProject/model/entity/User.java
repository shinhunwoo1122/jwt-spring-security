package com.project.testProject.model.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "users", schema = "testproject")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="user_id")
    private String userId;
    private String username;
    @Column
    private String password;
    @Column
    private String email;
    @Column
    private String role;
    @Column(name ="created_at")
    private LocalDateTime createdAt;
    @Column(name ="updated_at")
    private LocalDateTime updatedAt;

}
