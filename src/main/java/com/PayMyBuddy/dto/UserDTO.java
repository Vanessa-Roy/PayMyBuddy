package com.PayMyBuddy.dto;


import com.PayMyBuddy.controller.PayMyBuddyController;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Email;

import lombok.Data;


@Data
@PayMyBuddyController.PasswordMatches
public class UserDTO {
    @NotEmpty
    private String name;

    @NotEmpty
    private String password;

    @NotEmpty
    private String matchingPassword;

    @NotEmpty
    @Email
    private String email;
}

