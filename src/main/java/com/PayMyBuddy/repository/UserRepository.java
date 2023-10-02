package com.PayMyBuddy.repository;

import com.PayMyBuddy.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Load and save data relatives to users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find by email user.
     *
     * @param email the email to search for
     * @return the user
     */
    User findByEmail(String email);

    /**
     * Find all connections by user.
     *
     * @param user1 the email to search for
     * @return a list of users
     */
    @Query(value = "SELECT user1 FROM connections WHERE user2 = :user1 UNION SELECT user2 FROM connections WHERE user1 = :user1", nativeQuery = true)
    public Iterable<String> findAllConnectionsByEmail(@Param("user1") String user1);

    /**
     * Find all connections by user.
     *
     * @param user1    the email to search for
     * @param pageable the pageable
     * @return a list of users as pages
     */
    @Query(value = "SELECT user1 FROM connections WHERE user2 = :user1 UNION SELECT user2 FROM connections WHERE user1 = :user1", nativeQuery = true)
    public Page<String> findAllConnectionsByEmail(@Param("user1") String user1, Pageable pageable);


}
