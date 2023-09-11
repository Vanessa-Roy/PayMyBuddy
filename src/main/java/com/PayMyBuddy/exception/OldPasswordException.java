package com.PayMyBuddy.exception;

public class OldPasswordException extends Exception {
    public OldPasswordException() {
        super("The old password doesn't match with the registered password");
    }
}
