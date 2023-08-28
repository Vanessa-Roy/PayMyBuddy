package com.PayMyBuddy.exception;

public class PasswordMatchesException extends Exception {
    public PasswordMatchesException() {
        super("The matching password doesn't match with the password");
    }
}
