package com.PayMyBuddy.service;

import com.PayMyBuddy.constants.Fare;
import com.PayMyBuddy.exception.InvalidAmountException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralize every methods relatives to the fare.
 */
@Service
public class FareCalculatorService {

    /**
     * Calculate fare float.
     *
     * @param amount the amount of the transaction
     * @return the float fee charged to the sender user for the transaction
     * @throws InvalidAmountException the invalid amount exception
     */
    public float calculateFare(Float amount) throws InvalidAmountException {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException();
        }
        float result = amount * Fare.RATE_PER_TRANSACTION / 100;
        return new BigDecimal(result).setScale(2, RoundingMode.HALF_EVEN).floatValue();
    }
}
