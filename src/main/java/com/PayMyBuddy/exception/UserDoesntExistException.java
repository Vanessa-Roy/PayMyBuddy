package com.PayMyBuddy.exception;

public class UserDoesntExistException extends Exception {
    public UserDoesntExistException() {
        super("There is no account registered with this email");
    }
}
