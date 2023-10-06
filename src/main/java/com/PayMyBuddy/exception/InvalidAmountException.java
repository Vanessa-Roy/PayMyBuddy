package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when the amount about a transfer is invalid.
 */
public class InvalidAmountException extends Exception {
    /**
     * Instantiates a new Invalid amount exception.
     */
    public InvalidAmountException() {
        super("The amount must be positive");
    }
}
