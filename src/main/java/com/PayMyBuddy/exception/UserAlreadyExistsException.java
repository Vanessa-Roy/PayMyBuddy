package com.PayMyBuddy.exception;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException() {
        super("There is already an account registered with the same email");
    }
}
