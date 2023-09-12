package com.PayMyBuddy;

import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.MatchingPasswordException;
import com.PayMyBuddy.exception.OldPasswordException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
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
    private static PasswordDTO passwordDTO;

    @BeforeEach
    public void setUpPerTest() {
        userDTO = new UserDTO("nameTest", "passwordTest0!", "passwordTest0!", "email@test.com");
    }

    @Test
    public void saveUserDoesNotExistShouldPassTest() throws Exception {
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
    public void saveUserAlreadyExistsShouldFailTest() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> userServiceTest.saveUser(userDTO));
        assertEquals("There is already an account registered with the same email", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editPasswordShouldPassTest() throws Exception {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        passwordDTO = new PasswordDTO("passwordTest0!", "newPasswordTest0!", "newPasswordTest0!");
        when(userRepository.findByEmail("email@test.com")).thenReturn(user);
        when(passwordEncoder.matches(passwordDTO.getOldPassword(),user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(passwordDTO.getNewPassword())).thenReturn(passwordDTO.getNewPassword());

        userServiceTest.editPassword(passwordDTO);

        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(passwordEncoder, Mockito.times(1)).matches(passwordDTO.getOldPassword(),"passwordTest0!");
        verify(passwordEncoder, Mockito.times(1)).encode(passwordDTO.getNewPassword());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
        assertEquals("newPasswordTest0!",user.getPassword());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editPasswordWithoutSameOldNewPasswordShouldFailTest() {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        passwordDTO = new PasswordDTO("wrongPasswordTest0!", "newPasswordTest0!", "newPasswordTest0!");
        when(userRepository.findByEmail("email@test.com")).thenReturn(user);
        when(passwordEncoder.matches(passwordDTO.getOldPassword(),user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(OldPasswordException.class, () -> userServiceTest.editPassword(passwordDTO));
        assertEquals("The old password doesn't match with the registered password", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editPasswordWithoutSameNewMatchingPasswordShouldFailTest() {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        passwordDTO = new PasswordDTO("passwordTest0!", "newPasswordTest0!", "wrongNewPasswordTest0!");
        when(userRepository.findByEmail("email@test.com")).thenReturn(user);

        Exception exception = assertThrows(MatchingPasswordException.class, () -> userServiceTest.editPassword(passwordDTO));
        assertEquals("The matching password doesn't match with the password", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editNameShouldPassTest() {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        assertEquals("existingUser",user.getName());

        userServiceTest.editName(userDTO);

        assertEquals("nameTest",user.getName());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }
}
