package com.PayMyBuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "users")
public class User {

    @Id
    private String email;

    @Column(nullable = false, columnDefinition = "float default 0")
    private Float balance;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

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
