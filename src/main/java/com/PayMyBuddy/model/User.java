package com.PayMyBuddy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a user
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    /**
     * This defines the attribute email as a primary key.
     */
    @Id
    private String email;

    @Column(nullable = false, columnDefinition = "float default 0")
    private Float balance;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    /**
     * This defines the relationship between two users.
     */
    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "connections",
            joinColumns = @JoinColumn(name = "user1"),
            inverseJoinColumns = @JoinColumn(name = "user2")
    )
    private List<User> connections = new ArrayList<>();
}
