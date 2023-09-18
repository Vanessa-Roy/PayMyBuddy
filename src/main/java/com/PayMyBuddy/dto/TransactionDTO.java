package com.PayMyBuddy.dto;

import com.PayMyBuddy.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private LocalDate date;

    private String description;

    private float amount;

    private User connections;
}
