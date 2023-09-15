package com.PayMyBuddy.exception;

public class NotExistingConnection extends Exception {
    public NotExistingConnection() {
        super("The connection doesn't exist between these two users");
    }
}
