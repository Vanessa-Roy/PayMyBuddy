package com.PayMyBuddy.exception;

public class EmailRegexException extends Exception {

    public EmailRegexException() {
        super("the email doesn't respect the format");
    }
}
