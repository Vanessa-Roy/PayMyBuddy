package com.PayMyBuddy.service;

import com.PayMyBuddy.constants.Fare;
import com.PayMyBuddy.exception.InvalidAmountException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculatorService {

    public float calculateFare(Float amount) throws InvalidAmountException {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException();
        }
        float result = amount * Fare.RATE_PER_TRANSACTION / 100;
        return new BigDecimal(result).setScale(2, RoundingMode.HALF_EVEN).floatValue();
    }
}
