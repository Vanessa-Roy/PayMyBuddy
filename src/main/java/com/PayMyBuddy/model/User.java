package com.PayMyBuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent an user
 */
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private String email;

    @Column(nullable = false)
    private float balance;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @ManyToMany(
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "connections",
            joinColumns = @JoinColumn(name = "user1"),
            inverseJoinColumns = @JoinColumn(name = "user2")
    )
    private List<User> connections = new ArrayList<>();

}
