package com.PayMyBuddy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    private LocalDate date;

    private String description;

    private float amount;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(name="senderUser")
    private User senderUser;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(name="receiverUser")
    private User receiverUser;
}
