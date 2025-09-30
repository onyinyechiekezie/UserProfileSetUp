package com.userprofilesetup.data.models;

import com.userprofilesetup.data.enums.UserStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    private long id;
    private String email;
    private String password;
    private UserStatus status;
    private String verificationToken;

}
