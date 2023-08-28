package com.PayMyBuddy.validator;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.EmailRegexException;
import com.PayMyBuddy.exception.NotBlankAndEmptyException;
import com.PayMyBuddy.exception.PasswordMatchesException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserValidator {

    public void isValid(UserDTO user) throws PasswordMatchesException, EmailRegexException, NotBlankAndEmptyException {
        if (!user.getPassword().equals(user.getMatchingPassword())) {
            throw new PasswordMatchesException();
        } else if (!(Pattern.compile("^(.+)@(\\S+)$")
                .matcher(user.getEmail())
                .matches())) {
            throw new EmailRegexException();
        } else if (user.getName().isBlank() || user.getName().isEmpty()) {
            throw new NotBlankAndEmptyException("name");
        } else if (user.getPassword().isBlank() || user.getPassword().isEmpty()) {
            throw new NotBlankAndEmptyException("password");
        }
    }
}
