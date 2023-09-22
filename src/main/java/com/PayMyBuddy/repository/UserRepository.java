package com.PayMyBuddy.repository;

import com.PayMyBuddy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByEmail(String email);

    @Query(value = "SELECT user1 FROM connections WHERE user2 = :user1 UNION SELECT user2 FROM connections WHERE user1 = :user1", nativeQuery = true)
    public Iterable<String> findAllConnectionsByEmail(@Param("user1") String user1);

    @Query(value = "SELECT user1 FROM connections WHERE user2 = :user1 UNION SELECT user2 FROM connections WHERE user1 = :user1", nativeQuery = true)
    public Page<String> findAllConnectionsByEmail(@Param("user1") String user1, Pageable pageable);


}
