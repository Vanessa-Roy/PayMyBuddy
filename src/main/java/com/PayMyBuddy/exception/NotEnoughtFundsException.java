package com.PayMyBuddy.exception;

public class NotEnoughtFundsException extends Exception {
    public NotEnoughtFundsException() {
        super("The amount on your balance is not sufficient");
    }
}