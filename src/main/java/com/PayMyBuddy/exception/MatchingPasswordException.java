package com.PayMyBuddy.exception;

public class MatchingPasswordException extends Exception {
    public MatchingPasswordException() {
        super("The matching password doesn't match with the password");
    }
}
