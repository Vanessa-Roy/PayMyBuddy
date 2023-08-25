package com.PayMyBuddy.repository;

import com.PayMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Load and save data relatives to the users with database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByEmail(String email);

}
