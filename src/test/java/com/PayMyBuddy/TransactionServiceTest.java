package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.InvalidAmountException;
import com.PayMyBuddy.exception.NotEnoughtFundsException;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TransactionServiceTest {

    @InjectMocks
    private static TransactionService transactionServiceTest;
    @Mock
    private static UserRepository userRepository;
    @Mock
    private static TransactionRepository transactionRepository;

    private static User user;
    private static UserDTO userDTO;

    @BeforeEach
    public void setUpPerTest() {
        userDTO = new UserDTO("nameTest", "passwordTest0!", "passwordTest0!", "email@test.com");
    }
    @Test
    @WithMockUser(username = "email@test.com")
    public void withdrawShouldPassTest() throws NotEnoughtFundsException, InvalidAmountException {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        assertEquals(100f,user.getBalance());

        transactionServiceTest.withdraw(50f);

        assertEquals(50f,user.getBalance());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void withdrawWithAmountGreaterThanBalanceShouldFailTest() {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        assertEquals(100f,user.getBalance());

        Exception exception = assertThrows(NotEnoughtFundsException.class, () -> transactionServiceTest.withdraw(150f));
        assertEquals("The amount on your balance is not sufficient", exception.getMessage());
        assertEquals(100f,user.getBalance());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    public void withdrawWithNegativeAmountShouldFailTest() {
        Exception exception = assertThrows(InvalidAmountException.class, () -> transactionServiceTest.withdraw(-50f));
        assertEquals("The amount must be positive", exception.getMessage());
        verify(userRepository, Mockito.never()).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void depositShouldPassTest() throws InvalidAmountException {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        assertEquals(100f,user.getBalance());

        transactionServiceTest.deposit(50f);

        assertEquals(150f,user.getBalance());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    public void depositWithNegativeAmountShouldFailTest() {
        Exception exception = assertThrows(InvalidAmountException.class, () -> transactionServiceTest.deposit(-50f));
        assertEquals("The amount must be positive", exception.getMessage());
        verify(userRepository, Mockito.never()).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }
}
