package com.userprofilesetup.data.models;

import com.userprofilesetup.data.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Column(name = "verification_token")
    private String verificationToken;
    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;


}
