package com.PayMyBuddy.model;

import lombok.Data;

@Data
public class ConnectionId {
    private User sourceUser;
    private User friendUser;
}
