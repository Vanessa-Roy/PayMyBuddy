package com.PayMyBuddy.repository;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findBySenderUser(User user);

    List<Transaction> findByReceiverUser(User user);
}
