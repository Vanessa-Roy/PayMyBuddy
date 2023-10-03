package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.*;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralize every methods relatives to the user.
 */
@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Load user by username user.
     *
     * @param email the email to search for
     * @return the user
     */
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Save user.
     *
     * @param userDto the user to create
     * @throws Exception the exception
     */
    public void saveUser(UserDTO userDto) throws Exception {

        User existingUser = loadUserByUsername(userDto.getEmail());

        if(existingUser != null){
            throw new UserAlreadyExistsException();
        }

        if (!userDto.getPassword().equals(userDto.getMatchingPassword())) {
            throw new MatchingPasswordException();
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setBalance(0f);

        userRepository.save(user);
        logger.info("the user {} has been created", user.getEmail());
    }

    /**
     * Map to user dto user dto.
     *
     * @param user the user
     * @return the user dto
     */
    public UserDTO mapToUserDto(User user){
        UserDTO userDto = new UserDTO();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setBalance(user.getBalance());
        userDto.setConnections(user.getConnections());
        return userDto;
    }

    /**
     * Edit name.
     *
     * @param nameUser    the new name user
     * @param currentUser the current user
     */
    public void editName(String nameUser, User currentUser) {

        currentUser.setName(nameUser);

        userRepository.save(currentUser);
        logger.info("the name's user {} has been updated", currentUser.getEmail());
    }

    /**
     * Edit password.
     *
     * @param passwordDTO the password dto
     * @param currentUser the current user
     * @throws MatchingPasswordException the matching password exception
     * @throws OldPasswordException      the old password exception
     */
    public void editPassword(PasswordDTO passwordDTO, User currentUser) throws MatchingPasswordException, OldPasswordException {

        if (!passwordDTO.getNewPassword().equals(passwordDTO.getMatchingPassword())) {
            throw new MatchingPasswordException();
        }

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(),currentUser.getPassword())) {
            throw new OldPasswordException();
        }

        currentUser.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(currentUser);
        logger.info("the password's user {} has been updated", currentUser.getEmail());
    }

    /**
     * Gets connections.
     *
     * @param user the user to search for
     * @return a list of users
     */
    public List<UserDTO> getConnections(UserDTO user) {
        Iterable<String> connectionsEmails = userRepository.findAllConnectionsByEmail(user.getEmail());
        List<UserDTO> connections = new ArrayList<>();
        connectionsEmails.forEach(email -> connections.add(mapToUserDto(userRepository.findByEmail(email))));
        return connections;
    }

    /**
     * Gets connections.
     *
     * @param userEmail the user email to search for
     * @param pageable  the pageable
     * @return a list of users as pages
     */
    public Page<UserDTO> getConnections(String userEmail, Pageable pageable) {

        Page<String> connections = userRepository.findAllConnectionsByEmail(userEmail, pageable);
        List<UserDTO> connectionsDTO = new ArrayList<>();

        for (String connection : connections) {
            connectionsDTO.add(mapToUserDto(userRepository.findByEmail(connection)));
        }

        return new PageImpl<>(connectionsDTO, pageable, connections.getTotalElements());
    }

    /**
     * Add connection.
     *
     * @param emailUser1 the current user
     * @param emailUser2 the user to add as connection
     * @throws AlreadyExistingConnection the already existing connection
     * @throws UserDoesntExistException  the user doesnt exist exception
     */
    public void addConnection(String emailUser1, String emailUser2) throws AlreadyExistingConnection, UserDoesntExistException {
        User user1 = userRepository.findByEmail(emailUser1);

        User user2 = userRepository.findByEmail(emailUser2);

        if(user1 == null || user2 == null){
            throw new UserDoesntExistException();
        }

        if(user2.getConnections().contains(user1) || user1.getConnections().contains(user2) || user2.equals(user1)) {
            throw new AlreadyExistingConnection();
        }

        user1.getConnections().add(user2);

        userRepository.save(user1);
        logger.info("the connection between user {} and user {} has been created", user1.getEmail(), user2.getEmail());
    }

    /**
     * Delete connection.
     *
     * @param emailUser1 the current user
     * @param emailUser2 the user to delete as connection
     * @throws NotExistingConnection the not existing connection
     */
    public void deleteConnection(String emailUser1, String emailUser2) throws NotExistingConnection {
        User user1 = userRepository.findByEmail(emailUser1);

        User user2 = userRepository.findByEmail(emailUser2);

        if (user2.getConnections().contains(user1) || user1.getConnections().contains(user2)) {
            if (user2.getConnections().contains(user1)) {
                user2.getConnections().remove(user1);
                userRepository.save(user2);
            } else {
                user1.getConnections().remove(user2);
                userRepository.save(user1);
            }
            logger.info("the connection between user {} and user {} has been deleted", user1.getEmail(), user2.getEmail());
        } else {
            logger.error("the connection between user {} and user {} doesn't exist", user1.getEmail(), user2.getEmail());
            throw new NotExistingConnection();
        }
    }

}
