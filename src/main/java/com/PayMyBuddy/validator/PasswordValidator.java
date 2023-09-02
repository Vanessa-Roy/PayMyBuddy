package com.PayMyBuddy.validator;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.PasswordMatchesException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    public void isValid(UserDTO user) throws PasswordMatchesException {
        if (!user.getPassword().equals(user.getMatchingPassword())) {
            throw new PasswordMatchesException();
        }
    }
}
