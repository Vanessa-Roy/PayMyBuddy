package com.PayMyBuddy.dto;

import com.PayMyBuddy.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        final TransactionDTO other = (TransactionDTO) o;


        if (this.date != other.date) {
            return false;
        }

        if (!Objects.equals(this.description, other.description)) {
            return false;
        }

        if (this.amount != other.amount) {
            return false;
        }

        if (!Objects.equals(this.connections, other.connections)) {
            return false;
        }

        return true;
    }
}
