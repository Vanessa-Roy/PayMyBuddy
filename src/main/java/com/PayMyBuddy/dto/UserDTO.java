package com.PayMyBuddy.dto;

import com.PayMyBuddy.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @Pattern(regexp = "^(?=.*[A-Za-z]).*$", message = "name must contain letters")
    @Size(min = 2, max = 20, message = "the size must be between 2 and 20")
    private String name;
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$",
            message = "password must contain 8 characters with at least one digit, one special character, one lowercase letter and one uppercase letter"
    )
    private String password;
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$",
            message = "password must contain 8 characters with at least one digit, one special character, one lowercase letter and one uppercase letter"
    )
    private String matchingPassword;

    @Email
    private String email;

    @Min(value = 0, message = "balance must contain digits")
    private Float balance;

    private List<User> connections = new ArrayList<>();

    public UserDTO(String name, String password, String matchingPassword, String email) {
        this.name = name;
        this.password = password;
        this.matchingPassword = matchingPassword;
        this.email = email;
    }
}

