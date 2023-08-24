package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public User registerNewUserAccount(UserDTO userDto) throws UserAlreadyExistException {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }
        User user = objectMapper.convertValue(userDto, User.class);
        user.setBalance((float) 0);
        logger.info("the user has been created");

        return userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return userRepository.findById(email).isPresent();
    }

}
