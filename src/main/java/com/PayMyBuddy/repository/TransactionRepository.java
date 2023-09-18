package com.PayMyBuddy.repository;

import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query(value = "SELECT * FROM transactions WHERE receiver_user = :user2", nativeQuery = true)
    public Iterable<String> findTransactionsByUser2(@Param("user2") String user2);

}
