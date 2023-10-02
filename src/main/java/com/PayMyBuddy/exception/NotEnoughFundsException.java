package com.PayMyBuddy.exception;

/**
 * Represents an exception thrown when the amount about a transfer is too high.
 */
public class NotEnoughFundsException extends Exception {
    /**
     * Instantiates a new Not enough funds exception.
     */
    public NotEnoughFundsException() {
        super("The amount on your balance is not sufficient");
    }
}