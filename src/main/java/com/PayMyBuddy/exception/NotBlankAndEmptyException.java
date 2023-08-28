package com.PayMyBuddy.exception;

public class NotBlankAndEmptyException extends Exception {
    public NotBlankAndEmptyException(String argument) {
        super("This argument \"" + argument + "\" is required");
    }
}
