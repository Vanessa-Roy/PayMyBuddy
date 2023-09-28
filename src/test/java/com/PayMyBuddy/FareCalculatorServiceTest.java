package com.PayMyBuddy;

import com.PayMyBuddy.exception.InvalidAmountException;
import com.PayMyBuddy.service.FareCalculatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

    @InjectMocks
    private static FareCalculatorService fareCalculatorServiceTest;

    @Test
    public void calculateFareWithValidAmountShouldReturnFee() throws InvalidAmountException {

        assertEquals(0.5f, fareCalculatorServiceTest.calculateFare(100f));

    }

    @Test
    public void calculateFareWithValidAmountShouldReturnTwoDecimals() throws InvalidAmountException {

        assertEquals(0.01f, fareCalculatorServiceTest.calculateFare(1.52f));

    }

    @Test
    public void calculateFareWithNullAmountShouldNotReturnFee() throws InvalidAmountException {

        assertThrows(InvalidAmountException.class, () -> fareCalculatorServiceTest.calculateFare(null));

    }

    @Test
    public void calculateFareWithVInvalidAmountShouldNotReturnFee() throws InvalidAmountException {

        assertThrows(InvalidAmountException.class, () -> fareCalculatorServiceTest.calculateFare(-10f));

    }
}
