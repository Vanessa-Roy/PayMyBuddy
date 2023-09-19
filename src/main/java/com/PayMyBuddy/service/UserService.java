package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.*;
import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.validator.PasswordValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    public void saveUser(UserDTO userDto) throws Exception {

        User existingUser = loadUserByUsername(userDto.getEmail());

        if(existingUser != null){
            throw new UserAlreadyExistsException();
        }

        passwordValidator.isValid(userDto);

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setBalance(0f);

        userRepository.save(user);
        logger.info("the user {} has been created", user.getEmail());
    }

    public UserDTO mapToUserDto(User user){
        UserDTO userDto = new UserDTO();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setBalance(user.getBalance());
        userDto.setConnections(user.getConnections());
        return userDto;
    }

    public void editName(UserDTO userDto) {
        User existingUser = this.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        existingUser.setName(userDto.getName());

        userRepository.save(existingUser);
        logger.info("the name's user {} has been updated", existingUser.getEmail());
    }

    public void editPassword(PasswordDTO passwordDTO) throws MatchingPasswordException, OldPasswordException {
        User existingUser = this.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!passwordDTO.getNewPassword().equals(passwordDTO.getMatchingPassword())) {
            throw new MatchingPasswordException();
        }

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(),existingUser.getPassword())) {
            throw new OldPasswordException();
        }

        existingUser.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(existingUser);
        logger.info("the password's user {} has been updated", existingUser.getEmail());
    }

    public void withdraw(Float amount) throws NotEnoughtFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        User existingUser = this.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Float currentUserBalance = existingUser.getBalance();
        float newUserBalance = currentUserBalance - amount;
        if (newUserBalance < 0) {
            throw new NotEnoughtFundsException();
        }
        existingUser.setBalance(newUserBalance);
        userRepository.save(existingUser);
        logger.info("the balance's user {} has been updated", existingUser.getEmail());
    }

    public void deposit(Float amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        User existingUser = this.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Float currentUserBalance = existingUser.getBalance();
        float newUserBalance = currentUserBalance + amount;
        existingUser.setBalance(newUserBalance);
        System.out.println(existingUser.getBalance());
        userRepository.save(existingUser);
        logger.info("the balance's user {} has been updated", existingUser.getEmail());
    }

    public void addConnection(String emailUser1, String emailUser2) throws AlreadyExistingConnection, UserDoesntExistException {
        User user1 = userRepository.findByEmail(emailUser1);

        User user2 = userRepository.findByEmail(emailUser2);

        if(user1 == null || user2 == null){
            throw new UserDoesntExistException();
        }

        if(user2.getConnections().contains(user1) || user1.getConnections().contains(user2) || user2.equals(user1)) {
            throw new AlreadyExistingConnection();
        }

        user1.getConnections().add(user2);

        userRepository.save(user1);
        logger.info("the connection between user {} and user {} has been created", user1.getEmail(), user2.getEmail());
    }

    public List<UserDTO> getConnection(UserDTO user) {
        Iterable<String> connections1 = userRepository.findConnectionsByUser2(user.getEmail());
        List<User> connections2 = user.getConnections();
        List<UserDTO> connections = new ArrayList<>();
        connections1.forEach(connection1 -> connections.add(mapToUserDto(userRepository.findByEmail(connection1))));
        connections2.forEach(connection2 -> connections.add(mapToUserDto(connection2)));
        return connections;
    }

    public List<TransactionDTO> getTransaction(UserDTO user) {
        List<TransactionDTO> transactions = new ArrayList<>();

        List<Transaction> transaction1 = transactionRepository.findByReceiverUser(userRepository.findByEmail(user.getEmail()));

        for (Transaction transaction : transaction1) {
            TransactionDTO transaction1DTO = new TransactionDTO();
            transaction1DTO.setConnections(transaction.getSenderUser());
            transaction1DTO.setDate(transaction.getDate());
            transaction1DTO.setAmount(transaction.getAmount());
            transaction1DTO.setDescription(transaction.getDescription());
            transactions.add(transaction1DTO);
        }

        List<Transaction> transaction2 = transactionRepository.findBySenderUser(userRepository.findByEmail(user.getEmail()));

        for (Transaction transaction : transaction2) {
            TransactionDTO transaction2DTO = new TransactionDTO();
            transaction2DTO.setConnections(transaction.getReceiverUser());
            transaction2DTO.setDate(transaction.getDate());
            transaction2DTO.setAmount(-transaction.getAmount());
            transaction2DTO.setDescription(transaction.getDescription());
            transactions.add(transaction2DTO);
        }

        transactions.sort(Comparator.comparing(TransactionDTO::getDate).reversed());

        return transactions;
    }

    public Page<UserDTO> getPaginatedConnection(Pageable pageable, List<UserDTO> connections) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<UserDTO> list;

        if (connections.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, connections.size());
            list = connections.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), connections.size());
    }

    public Page<TransactionDTO> getPaginatedTransactions(Pageable pageable, List<TransactionDTO> transactions) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<TransactionDTO> list;

        if (transactions.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, transactions.size());
            list = transactions.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), transactions.size());
    }

    public void deleteConnection(String emailUser1, String emailUser2) throws NotExistingConnection {
        User user1 = userRepository.findByEmail(emailUser1);

        User user2 = userRepository.findByEmail(emailUser2);

        if (user2.getConnections().contains(user1) || user1.getConnections().contains(user2)) {
            if (user2.getConnections().contains(user1)) {
                user2.getConnections().remove(user1);
                userRepository.save(user2);
            } else {
                user1.getConnections().remove(user2);
                userRepository.save(user1);
            }
            logger.info("the connection between user {} and user {} has been deleted", user1.getEmail(), user2.getEmail());
        } else {
            logger.error("the connection between user {} and user {} doesn't exist", user1.getEmail(), user2.getEmail());
            throw new NotExistingConnection();
        }
    }

    @Transactional
    public void sendMoney(float amount, String description, String receiverEmail, String senderEmail) throws InvalidAmountException, UserDoesntExistException, NotEnoughtFundsException, NotExistingConnection {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }

        User senderUser = userRepository.findByEmail(senderEmail);

        User receiverUser = userRepository.findByEmail(receiverEmail);

        if(senderEmail == null || receiverEmail == null){
            throw new UserDoesntExistException();
        }

        if(senderUser.equals(receiverUser)) {
            throw new NotExistingConnection();
        }

        if(senderUser.getConnections().contains(receiverUser) || receiverUser.getConnections().contains(senderUser)) {
            Float currentSenderUserBalance = senderUser.getBalance();
            Float currentReceiverUserBalance = receiverUser.getBalance();

            float newSenderUserBalance = currentSenderUserBalance - amount;

            if (newSenderUserBalance < 0) {
                throw new NotEnoughtFundsException();
            }

            float newReceiverUserBalance = currentReceiverUserBalance + amount;

            senderUser.setBalance(newSenderUserBalance);
            receiverUser.setBalance(newReceiverUserBalance);

            userRepository.save(senderUser);
            userRepository.save(receiverUser);

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setReceiverUser(receiverUser);
            transaction.setSenderUser(senderUser);
            transaction.setDate(LocalDate.now());
            transaction.setDescription(description);

            transactionRepository.save(transaction);

            logger.info("The money transfer about {}$ between the users {} and {} has been made", amount, receiverEmail, senderEmail);
            logger.info("The transaction about {}$ between the users {} and {} has been created", amount, receiverEmail, senderEmail);
        } else {
            throw new NotExistingConnection();
        }
    }

}
