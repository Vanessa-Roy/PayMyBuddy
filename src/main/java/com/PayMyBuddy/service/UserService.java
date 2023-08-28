package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.EmailRegexException;
import com.PayMyBuddy.exception.NotBlankAndEmptyException;
import com.PayMyBuddy.exception.PasswordMatchesException;
import com.PayMyBuddy.exception.UserAlreadyExistException;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.validator.UserValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    public void saveUser(UserDTO userDto) throws Exception {

        userValidator.isValid(userDto);

        User existingUser = findUserByEmail(userDto.getEmail());

        if(existingUser != null){
            throw new UserAlreadyExistException();
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setBalance((float) 0);

        userRepository.save(user);
        logger.info("the user has been created");
    }

    private UserDTO mapToUserDto(User user){
        UserDTO userDto = new UserDTO();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private boolean emailExists(String email) {
        return userRepository.findById(email).isPresent();
    }

}
