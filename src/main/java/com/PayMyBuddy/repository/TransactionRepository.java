package com.PayMyBuddy.repository;

import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Load and save data relatives to transactions.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    /**
     * Find by sender user.
     *
     * @param user the senderUser
     * @return a list of transactions
     */
    List<Transaction> findBySenderUser(User user);

    /**
     * Find by sender user or receiver user.
     *
     * @param senderUser   the sender user
     * @param receiverUser the receiver user
     * @param pageable     the pageable
     * @return a list of transaction as pages
     */
    Page<Transaction> findBySenderUserOrReceiverUser(User senderUser, User receiverUser, Pageable pageable);

}
