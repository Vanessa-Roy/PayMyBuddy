package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.*;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.validator.PasswordValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    public void saveUser(UserDTO userDto) throws Exception {

        User existingUser = loadUserByUsername(userDto.getEmail());

        if(existingUser != null){
            throw new UserAlreadyExistsException();
        }

        passwordValidator.isValid(userDto);

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setBalance(0f);

        userRepository.save(user);
        logger.info("the user {} has been created", user.getEmail());
    }

    public UserDTO mapToUserDto(User user){
        UserDTO userDto = new UserDTO();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setBalance(user.getBalance());
        userDto.setConnections(user.getConnections());
        return userDto;
    }

    public void editName(UserDTO userDto) {
        User existingUser = this.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        existingUser.setName(userDto.getName());

        userRepository.save(existingUser);
        logger.info("the name's user {} has been updated", existingUser.getEmail());
    }

    public void editPassword(PasswordDTO passwordDTO) throws MatchingPasswordException, OldPasswordException {
        User existingUser = this.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!passwordDTO.getNewPassword().equals(passwordDTO.getMatchingPassword())) {
            throw new MatchingPasswordException();
        }

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(),existingUser.getPassword())) {
            throw new OldPasswordException();
        }

        existingUser.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(existingUser);
        logger.info("the password's user {} has been updated", existingUser.getEmail());
    }

    public List<UserDTO> getConnections(UserDTO user) {
        Iterable<String> connectionsEmails = userRepository.findAllConnectionsByEmail(user.getEmail());
        List<UserDTO> connections = new ArrayList<>();
        connectionsEmails.forEach(email -> connections.add(mapToUserDto(userRepository.findByEmail(email))));
        return connections;
    }

    public Page<UserDTO> getConnections(String userEmail, Pageable pageable) {

        Page<String> connections = userRepository.findAllConnectionsByEmail(userEmail, pageable);
        List<UserDTO> connectionsDTO = new ArrayList<>();

        for (String connection : connections) {
            connectionsDTO.add(mapToUserDto(userRepository.findByEmail(connection)));
        }

        return new PageImpl<>(connectionsDTO, pageable, connections.getTotalElements());
    }

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
