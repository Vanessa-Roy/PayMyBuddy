package com.PayMyBuddy.dto;


import com.PayMyBuddy.controller.PayMyBuddyController;
import com.PayMyBuddy.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.junit.Ignore;


@Data
@PayMyBuddyController.PasswordMatches
@JsonIgnoreProperties(value = { "matchingPassword" })
public class UserDTO {
    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String password;
    private String matchingPassword;

    @NotNull
    @NotEmpty
    @PayMyBuddyController.ValidEmail
    private String email;
}

