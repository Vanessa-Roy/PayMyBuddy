package com.PayMyBuddy;

import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.InvalidAmountException;
import com.PayMyBuddy.exception.NotEnoughtFundsException;
import com.PayMyBuddy.exception.NotExistingConnection;
import com.PayMyBuddy.exception.UserDoesntExistException;
import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.ArrayList;
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
    public void withdrawShouldPassTest() throws NotEnoughtFundsException, InvalidAmountException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
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
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
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
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
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

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionSenderUserShouldPass() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,user,user2);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findByReceiverUser(user)).thenReturn(new ArrayList<>());
        when(transactionRepository.findBySenderUser(user)).thenReturn(new ArrayList<>(List.of(transaction)));

        List<TransactionDTO> result = transactionServiceTest.getTransaction(userDTO);

        assertEquals(1, result.size());
        assertEquals(-10f, result.get(0).getAmount());
        assertEquals("transactionTest", result.get(0).getDescription());
        assertEquals(user2, result.get(0).getConnections());
        verify(transactionRepository, Mockito.times(1)).findBySenderUser(user);
        verify(transactionRepository, Mockito.times(1)).findByReceiverUser(user);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionReceiveUserShouldPass() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,user2,user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findByReceiverUser(user)).thenReturn(new ArrayList<>(List.of(transaction)));
        when(transactionRepository.findBySenderUser(user)).thenReturn(new ArrayList<>());

        List<TransactionDTO> result = transactionServiceTest.getTransaction(userDTO);

        assertEquals(1, result.size());
        assertEquals(10f, result.get(0).getAmount());
        assertEquals("transactionTest", result.get(0).getDescription());
        assertEquals(user2, result.get(0).getConnections());
        verify(transactionRepository, Mockito.times(1)).findBySenderUser(user);
        verify(transactionRepository, Mockito.times(1)).findByReceiverUser(user);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getTransactionWithoutTransactionShouldPass() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(transactionRepository.findByReceiverUser(any(User.class))).thenReturn(new ArrayList<>());
        when(transactionRepository.findBySenderUser(any(User.class))).thenReturn(new ArrayList<>());
        List<TransactionDTO> expectedResult = new ArrayList<>();

        assertEquals(expectedResult,transactionServiceTest.getTransaction(userDTO));

        verify(transactionRepository, Mockito.times(1)).findBySenderUser(user);
        verify(transactionRepository, Mockito.times(1)).findByReceiverUser(user);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyUser1toUser2ShouldPass() throws NotExistingConnection, UserDoesntExistException, InvalidAmountException, NotEnoughtFundsException {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,user,user2);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        transactionServiceTest.sendMoney(10f, "transactionTest", user2.getEmail(), user.getEmail());

        assertEquals(90f, user.getBalance());
        assertEquals(110f, user2.getBalance());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.times(1)).save(user);
        verify(userRepository, Mockito.times(1)).save(user2);
        verify(transactionRepository, Mockito.times(1)).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyUser1toUser2WithNotExistingConnectionShouldFail() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>());
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,user,user2);
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
    public void sendMoneyToOneselfShouldFail() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        Transaction transaction = new Transaction(1,LocalDate.now(),"transactionTest",10f,user,user);
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
    public void sendMoneyWithInvalidAmountShouldFail() {
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
    public void sendMoneyWithInsufficientBalanceShouldFail() {
        user = new User("email@test.com",100f,"userTest","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"user2Test","passwordTest0!",new ArrayList<>(List.of(user)));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(NotEnoughtFundsException.class, () ->
                transactionServiceTest.sendMoney(200f, "transactionTest", user.getEmail(), user2.getEmail()));

        assertEquals("The amount on your balance is not sufficient", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
        verify(transactionRepository, Mockito.never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void sendMoneyWithUnknownReceiverUserShouldFail() throws NotExistingConnection, UserDoesntExistException, InvalidAmountException, NotEnoughtFundsException {
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
    public void sendMoneyWithUnknownSenderUserShouldFail() throws NotExistingConnection, UserDoesntExistException, InvalidAmountException, NotEnoughtFundsException {
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
