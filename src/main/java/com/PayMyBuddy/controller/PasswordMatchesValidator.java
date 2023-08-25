package com.PayMyBuddy.controller;

import com.PayMyBuddy.dto.UserDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class PasswordMatchesValidator implements ConstraintValidator<PayMyBuddyController.PasswordMatches, Object> {

    @Override
    public void initialize(PayMyBuddyController.PasswordMatches constraintAnnotation) {
    }
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
            UserDTO user = (UserDTO) obj;
            return user.getPassword().equals(user.getMatchingPassword());
    }

    public void isValid() {
    }
}
