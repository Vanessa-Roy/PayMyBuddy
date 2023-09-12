package com.PayMyBuddy.service;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.MatchingPasswordException;
import com.PayMyBuddy.exception.OldPasswordException;
import com.PayMyBuddy.exception.UserAlreadyExistsException;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.validator.PasswordValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
