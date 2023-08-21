package com.PayMyBuddy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "connections")
@IdClass(ConnectionId.class)
public class Connection {

    @Id
    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "sourceUser")
    private User sourceUser;

    @Id
    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "friendUser")
    private User friendUser;

}

