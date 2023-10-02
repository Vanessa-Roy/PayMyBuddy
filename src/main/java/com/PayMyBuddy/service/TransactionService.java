package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.exception.InvalidAmountException;
import com.PayMyBuddy.exception.NotEnoughFundsException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralize every methods relatives to the transaction.
 */
@Service
public class TransactionService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;


    @Autowired
    private FareCalculatorService fareCalculatorService;

    /**
     * Gets transactions.
     *
     * @param userEmail the email to search for
     * @param pageable  the pageable
     * @return a list of transactions as pages
     */
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

    /**
     * Send money.
     *
     * @param amount        the amount to transfer
     * @param description   the description about the transfer
     * @param receiverEmail the receiver email
     * @param senderEmail   the sender email
     * @throws InvalidAmountException   the invalid amount exception
     * @throws UserDoesntExistException the user doesnt exist exception
     * @throws NotEnoughFundsException the not enought funds exception
     * @throws NotExistingConnection    the not existing connection
     */
    @Transactional
    public void sendMoney(float amount, String description, String receiverEmail, String senderEmail) throws InvalidAmountException, UserDoesntExistException, NotEnoughFundsException, NotExistingConnection {
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
            float currentSenderUserBalance = senderUser.getBalance();
            float currentReceiverUserBalance = receiverUser.getBalance();
            float currentTransactionFee = fareCalculatorService.calculateFare(amount);

            float newSenderUserBalance = currentSenderUserBalance - amount - currentTransactionFee;

            if (newSenderUserBalance < 0) {
                throw new NotEnoughFundsException();
            }

            newSenderUserBalance = new BigDecimal(newSenderUserBalance).setScale(2, RoundingMode.HALF_EVEN).floatValue();

            float newReceiverUserBalance = currentReceiverUserBalance + amount;

            newReceiverUserBalance = new BigDecimal(newReceiverUserBalance).setScale(2, RoundingMode.HALF_EVEN).floatValue();

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
            transaction.setFee(currentTransactionFee);

            transactionRepository.save(transaction);

            logger.info("The money transfer about {}$ between the users {} and {} has been made", amount, receiverEmail, senderEmail);
            logger.info("The transaction about {}$ between the users {} and {} has been created", amount, receiverEmail, senderEmail);
        } else {
            throw new NotExistingConnection();
        }
    }

    /**
     * Withdraw.
     *
     * @param amount      the amount to transfer to the user's bank
     * @param currentUser the current user
     * @throws NotEnoughFundsException the not enough funds exception
     * @throws InvalidAmountException   the invalid amount exception
     */
    public void withdraw(Float amount, User currentUser) throws NotEnoughFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        float currentUserBalance = currentUser.getBalance();
        float newUserBalance = currentUserBalance - amount;
        if (newUserBalance < 0) {
            throw new NotEnoughFundsException();
        }
        currentUser.setBalance(newUserBalance);
        userRepository.save(currentUser);
        logger.info("the balance's user {} has been updated", currentUser.getEmail());
    }

    /**
     * Deposit.
     *
     * @param amount      the amount to transfer from the user's bank
     * @param currentUser the current user
     * @throws InvalidAmountException the invalid amount exception
     */
    public void deposit(Float amount, User currentUser) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        float currentUserBalance = currentUser.getBalance();
        float newUserBalance = currentUserBalance + amount;
        currentUser.setBalance(newUserBalance);
        userRepository.save(currentUser);
        logger.info("the balance's user {} has been updated", currentUser.getEmail());
    }

}
