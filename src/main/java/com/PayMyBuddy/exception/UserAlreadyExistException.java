package com.PayMyBuddy.exception;

public class UserAlreadyExistException extends Exception {
    public UserAlreadyExistException() {
        super("There is already an account registered with the same email");
    }
}
