package com.PayMyBuddy;

import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.InvalidAmountException;
import com.PayMyBuddy.exception.NotEnoughFundsException;
import com.PayMyBuddy.exception.NotExistingConnection;
import com.PayMyBuddy.exception.UserDoesntExistException;
import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.service.FareCalculatorService;
import com.PayMyBuddy.service.TransactionService;
import com.PayMyBuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static FareCalculatorService fareCalculatorService;
    @Mock
    private static UserService userService;
    @Mock
    private static UserRepository userRepository;
    @Mock
    private static TransactionRepository transactionRepository;

    private static User user;
    private static UserDTO userDTO;

    @BeforeEach
    public void setUpPerTest() {
        userDTO = new UserDTO("userTest", "passwordTest0!", "passwordTest0!", "email@test.com", 100f, new ArrayList<>());
    }
    @Test
    @WithMockUser(username = "email@test.com")
    public void withdrawShouldUpdateAttributeBalanceUserTest() throws NotEnoughFundsException, InvalidAmountException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        assertEquals(100f,user.getBalance());

        transactionServiceTest.withdraw(50f, user);

        assertEquals(50f,user.getBalance());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void withdrawWithAmountGreaterThanBalanceShouldNotUpdateAttributeBalanceUserTest() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        assertEquals(100f,user.getBalance());

        Exception exception = assertThrows(NotEnoughFundsException.class, () -> transactionServiceTest.withdraw(150f, user));
        assertEquals("The amount on your balance is not sufficient", exception.getMessage());
        assertEquals(100f,user.getBalance());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    public void withdrawWithNegativeAmountShouldNotUpdateAttributeBalanceUserTest() {
        Exception exception = assertThrows(InvalidAmountException.class, () -> transactionServiceTest.withdraw(-50f, user));
        assertEquals("The amount must be positive", exception.getMessage());
        verify(userRepository, Mockito.never()).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void depositShouldUpdateAttributeBalanceUserTest() throws InvalidAmountException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        assertEquals(100f,user.getBalance());

        transactionServiceTest.deposit(50f, user);

        assertEquals(150f,user.getBalance());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    public void depositWithNegativeAmountShouldNotUpdateAttributeBalanceUserTest() {
        Exception exception = assertThrows(InvalidAmountException.class, () -> transactionServiceTest.deposit(-50f, user));
        assertEquals("The amount must be positive", exception.getMessage());
        verify(userRepository, Mockito.never()).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionsSenderUserShouldShowAllTransactionsUserTest() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,0.05f,user,user2);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3))).thenReturn(new PageImpl<>(List.of(transaction)));
        List<TransactionDTO> expectedResult = new ArrayList<>(List.of(
                new TransactionDTO(
                        transaction.getDate(),
                        transaction.getDescription(),
                        -transaction.getAmount(),
                        transaction.getReceiverUser())
        ));

        Page<TransactionDTO> result = transactionServiceTest.getTransactions(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResult, result.getContent());
        verify(transactionRepository, Mockito.times(1)).findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionsReceiverUserShouldShowAllTransactionsUserTest() {
        user = new User("email@test.com", 100f, "userTest", "passwordTest0!", new ArrayList<>());
        User user2 = new User("email2@test.com", 100f, "user2Test", "passwordTest0!", new ArrayList<>(List.of(user)));
        Transaction transaction = new Transaction(1, LocalDate.now(), "transactionTest", 10f, 0.05f, user2, user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3))).thenReturn(new PageImpl<>(List.of(transaction)));
        List<TransactionDTO> expectedResult = new ArrayList<>(List.of(
                new TransactionDTO(
                        transaction.getDate(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        transaction.getSenderUser())
        ));

        Page<TransactionDTO> result = transactionServiceTest.getTransactions(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResult, result.getContent());
        verify(transactionRepository, Mockito.times(1)).findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionsShouldNotShowTransactionsOfOtherUsersTest() {
        user = new User("email@test.com", 100f, "userTest", "passwordTest0!", new ArrayList<>());
        Transaction transaction = new Transaction(1, LocalDate.now(), "transactionTest", 10f, 0.05f, new User(), new User());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3))).thenReturn(new PageImpl<>(List.of(transaction)));
        List<TransactionDTO> expectedResult = new ArrayList<>(List.of());

        Page<TransactionDTO> result = transactionServiceTest.getTransactions(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResult, result.getContent());
        verify(transactionRepository, Mockito.times(1)).findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionsWithoutTransactionShouldShowAnEmptyListTest() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3))).thenReturn(new PageImpl<>(Collections.emptyList()));
        List<TransactionDTO> expectedResult = new ArrayList<>(List.of());

        Page<TransactionDTO> result = transactionServiceTest.getTransactions(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(0, result.getTotalElements());
        assertEquals(expectedResult, result.getContent());
        verify(transactionRepository, Mockito.times(1)).findBySenderUserOrReceiverUser(user, user, PageRequest.of(0, 3));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyUser1toUser2ShouldUpdateAttributeBalanceBothUsersTest() throws NotExistingConnection, UserDoesntExistException, InvalidAmountException, NotEnoughFundsException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        when(fareCalculatorService.calculateFare(10f)).thenReturn(0.05f);

        transactionServiceTest.sendMoney(10f, "transactionTest", user2.getEmail(), user.getEmail());

        assertEquals(89.95f, user.getBalance());
        assertEquals(110f, user2.getBalance());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.times(1)).save(user);
        verify(userRepository, Mockito.times(1)).save(user2);
        verify(transactionRepository, Mockito.times(1)).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyUser1toUser2WithNotExistingConnectionShouldNotUpdateAttributeBalanceBothUsersTest() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(NotExistingConnection.class, () ->
                transactionServiceTest.sendMoney(10f, "transactionTest", user2.getEmail(), user.getEmail()));
        assertEquals("The connection doesn't exist between these two users", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyToOneselfShouldNotUpdateAttributeBalanceUserTest() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,0.05f,user,user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        Exception exception = assertThrows(NotExistingConnection.class, () ->
                transactionServiceTest.sendMoney(10f, "transactionTest", user.getEmail(), user.getEmail()));
        assertEquals("The connection doesn't exist between these two users", exception.getMessage());
        verify(userRepository, Mockito.times(2)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyWithInvalidAmountShouldNotUpdateAttributeBalanceBothUsersTest() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>());

        Exception exception = assertThrows(InvalidAmountException.class, () ->
                transactionServiceTest.sendMoney(-10f, "transactionTest", user.getEmail(), user2.getEmail()));

        assertEquals("The amount must be positive", exception.getMessage());
        verify(userRepository, Mockito.never()).findByEmail(user.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyWithInsufficientBalanceShouldNotUpdateAttributeBalanceBothUsersTest() throws InvalidAmountException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        when(fareCalculatorService.calculateFare(200f)).thenReturn(1.0f);

        Exception exception = assertThrows(NotEnoughFundsException.class, () ->
                transactionServiceTest.sendMoney(200f, "transactionTest", user.getEmail(), user2.getEmail()));

        assertEquals("The amount on your balance is not sufficient", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyWithUnknownReceiverUserShouldNotUpdateAttributeBalanceBothUsersTest() throws NotExistingConnection, UserDoesntExistException, InvalidAmountException, NotEnoughFundsException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail("email2@test.com")).thenReturn(null);

        Exception exception = assertThrows(UserDoesntExistException.class, () ->
                transactionServiceTest.sendMoney(10f, "transactionTest", "email2@test.com", user.getEmail()));

        assertEquals("There is no account registered with this email", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail("email2@test.com");
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyWithUnknownSenderUserShouldNotUpdateAttributeBalanceBothUsersTest() throws NotExistingConnection, UserDoesntExistException, InvalidAmountException, NotEnoughFundsException {
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail("email@test.com")).thenReturn(null);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(UserDoesntExistException.class, () ->
                transactionServiceTest.sendMoney(10f, "transactionTest", user2.getEmail(), "email@test.com"));

        assertEquals("There is no account registered with this email", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail("email@test.com");
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }
}
