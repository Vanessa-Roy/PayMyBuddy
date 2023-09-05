package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.UserAlreadyExistsException;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.service.UserService;
import com.PayMyBuddy.validator.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private static UserService userServiceTest;
    @Mock
    private static UserRepository userRepository;
    @Mock
    private static PasswordValidator passwordValidator;
    @Mock
    private static PasswordEncoder passwordEncoder;

    private static User user;
    private static UserDTO userDTO;


    @BeforeEach
    public void setUpPerTest() {
        userDTO = new UserDTO("nameTest", "passwordTest!", "passwordTest!", "email@test.com");
        user = new User();
    }

    @Test
    public void saveUserDoesNotExistTest() throws Exception {
        when(passwordValidator.isValid(userDTO)).thenReturn(true);
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn(userDTO.getPassword());

        userServiceTest.saveUser(userDTO);

        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(passwordValidator, Mockito.times(1)).isValid(userDTO);
        verify(passwordEncoder, Mockito.times(1)).encode(userDTO.getPassword());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    public void saveUserAlreadyExistsTest() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> userServiceTest.saveUser(userDTO));
        assertEquals("There is already an account registered with the same email", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
    }
}
