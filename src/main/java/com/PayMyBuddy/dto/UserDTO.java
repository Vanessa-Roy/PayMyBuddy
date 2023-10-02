package com.PayMyBuddy.dto;

import com.PayMyBuddy.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Represents a dto used to create or update a real user.
 */
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
    @Size(min = 5, max = 320, message = "the size must be between 5 and 320")
    private String email;

    @Min(value = 0, message = "balance must contain digits")
    private float balance;

    private List<User> connections = new ArrayList<>();

    /**
     * Instantiates a new User dto.
     *
     * @param displayName      the display name
     * @param password         the password
     * @param matchingPassword the matching password
     * @param email            the email
     */
    public UserDTO(String displayName, String password, String matchingPassword, String email) {
        this(displayName, email);
        this.password = password;
        this.matchingPassword = matchingPassword;
    }

    /**
     * Instantiates a new User dto.
     *
     * @param displayName the display name
     * @param email       the email
     */
    public UserDTO(String displayName, String email) {
        this.name = displayName;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        final UserDTO other = (UserDTO) o;

        if (!Objects.equals(this.name, other.name)) {
            return false;
        }

        if (!Objects.equals(this.password, other.password)) {
            return false;
        }

        if (!Objects.equals(this.matchingPassword, other.matchingPassword)) {
            return false;
        }

        if (!Objects.equals(this.email, other.email)) {
            return false;
        }

        if (this.balance != other.balance) {
            return false;
        }

        if (!Objects.equals(this.connections, other.connections)) {
            return false;
        }

        return true;
    }
}

