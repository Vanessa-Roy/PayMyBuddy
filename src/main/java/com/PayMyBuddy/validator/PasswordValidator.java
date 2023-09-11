package com.PayMyBuddy.validator;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.MatchingPasswordException;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class PasswordValidator {

    public boolean isValid(UserDTO user) throws MatchingPasswordException {
        if (!user.getPassword().equals(user.getMatchingPassword())) {
            throw new MatchingPasswordException();
        }
        return true;
    }
}
