package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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
