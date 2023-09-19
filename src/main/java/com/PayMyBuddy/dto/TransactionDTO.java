package com.PayMyBuddy.dto;

import com.PayMyBuddy.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    @DateTimeFormat
    private LocalDate date;

    @Size(max = 200, message = "the size must be max 200")
    private String description;

    @Min(0)
    private float amount;

    private User connections;
}
