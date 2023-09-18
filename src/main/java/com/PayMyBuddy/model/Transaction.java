package com.PayMyBuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    @Column(nullable = false)
    private LocalDate date;

    private String description;

    @Column(nullable = false)
    private float amount;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(name="sender_user")
    private User senderUser;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(name="receiver_user")
    private User receiverUser;
}
