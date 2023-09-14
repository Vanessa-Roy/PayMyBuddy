package com.PayMyBuddy.repository;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByEmail(String email);

    @Query(value = "SELECT user1 FROM connections WHERE user2 = :user2", nativeQuery = true)
    public Iterable<String> findConnectionsByUser2(@Param("user2") String user2);

}
