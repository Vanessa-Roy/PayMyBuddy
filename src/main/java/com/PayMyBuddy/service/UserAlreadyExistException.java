package com.PayMyBuddy.service;

public class UserAlreadyExistException extends Exception {
    public UserAlreadyExistException(String errorMessage) {
        super(errorMessage);
    }
}
