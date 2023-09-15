package com.PayMyBuddy.exception;

public class InvalidAmountException extends Exception {
    public InvalidAmountException() {
        super("The amount must be positive");
    }
}
