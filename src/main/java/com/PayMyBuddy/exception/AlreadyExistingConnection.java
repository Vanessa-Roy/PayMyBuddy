package com.PayMyBuddy.exception;

public class AlreadyExistingConnection extends Exception {
    public AlreadyExistingConnection() {
        super("The connection already exists between these two users");
    }
}
