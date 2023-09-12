package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.MatchingPasswordException;
import com.PayMyBuddy.validator.PasswordValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    private static PasswordValidator passwordValidatorTest;
    private static UserDTO user;

    @BeforeAll
    public static void setUp() {
        passwordValidatorTest = new PasswordValidator();
        user = new UserDTO();
        user.setPassword("passwordTest!");
    }

    @Test
    public void passwordValidatorIsValidWithSamePasswordShouldPassTest() throws MatchingPasswordException {
        user.setMatchingPassword("passwordTest!");

        assertTrue(passwordValidatorTest.isValid(user));
    }

    @Test
    public void passwordValidatorIsValidWithoutSamePasswordShouldFailTest() {
        user.setMatchingPassword("wrongPasswordTest!");

        Exception exception = assertThrows(MatchingPasswordException.class, () -> passwordValidatorTest.isValid(user));
        assertEquals("The matching password doesn't match with the password", exception.getMessage());
    }


}
