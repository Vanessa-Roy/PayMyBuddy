package com.PayMyBuddy.validator;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.PasswordMatchesException;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class PasswordValidator {

    public boolean isValid(UserDTO user) throws PasswordMatchesException {
        if (!user.getPassword().equals(user.getMatchingPassword())) {
            throw new PasswordMatchesException();
        }
        return true;
    }
}
