package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.exception.InvalidAmountException;
import com.PayMyBuddy.exception.NotEnoughtFundsException;
import com.PayMyBuddy.exception.NotExistingConnection;
import com.PayMyBuddy.exception.UserDoesntExistException;
import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Page<TransactionDTO> getTransactions(String userEmail, Pageable pageable) {

        User currentUser = userRepository.findByEmail(userEmail);
        Page<Transaction> transactions = transactionRepository.findBySenderUserOrReceiverUser(currentUser, currentUser, pageable);

        List<TransactionDTO> transactionsDTO = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction.getSenderUser().equals(currentUser)) {
                transactionsDTO.add(new TransactionDTO(
                        transaction.getDate(),
                        transaction.getDescription(),
                        -transaction.getAmount(), // we set a negative amount when the current user is the sender
                        transaction.getReceiverUser() // we set the connection without the current user
                        )
                );
            } else if (transaction.getReceiverUser().equals(currentUser)) {
                transactionsDTO.add(new TransactionDTO(
                                transaction.getDate(),
                                transaction.getDescription(),
                                transaction.getAmount(),
                                transaction.getSenderUser()
                        )
                );
            }
        }

        return new PageImpl<>(transactionsDTO, pageable, transactions.getTotalElements());
    }

    @Transactional
    public void sendMoney(float amount, String description, String receiverEmail, String senderEmail) throws InvalidAmountException, UserDoesntExistException, NotEnoughtFundsException, NotExistingConnection {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }

        User senderUser = userRepository.findByEmail(senderEmail);

        User receiverUser = userRepository.findByEmail(receiverEmail);

        if(senderUser == null || receiverUser == null){
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

    public void withdraw(Float amount) throws NotEnoughtFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        User existingUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
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
        User existingUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        Float currentUserBalance = existingUser.getBalance();
        float newUserBalance = currentUserBalance + amount;
        existingUser.setBalance(newUserBalance);
        System.out.println(existingUser.getBalance());
        userRepository.save(existingUser);
        logger.info("the balance's user {} has been updated", existingUser.getEmail());
    }

}
