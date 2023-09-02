package com.PayMyBuddy.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class UserDTO {
    @Pattern(regexp = "[a-z-A-Z]*", message = "name must contain only letters")
    @Size(min = 2, max = 20)
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
}

