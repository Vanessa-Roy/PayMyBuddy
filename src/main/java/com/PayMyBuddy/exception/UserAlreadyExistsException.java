package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when a user has been already created.
 */
public class UserAlreadyExistsException extends Exception {
    /**
     * Instantiates a new User already exists exception.
     */
    public UserAlreadyExistsException() {
        super("There is already an account registered with the same email");
    }
}
