package com.PayMyBuddy.dto;

/**
 * Represents a dto used to collect a user's email.
 */
public record GithubApiEmailDTO(String email, String primary, String verified, String visibility) {
}
