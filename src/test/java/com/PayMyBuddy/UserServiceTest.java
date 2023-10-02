package com.PayMyBuddy;

import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.exception.*;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static PasswordEncoder passwordEncoder;
    private static User user;
    private static UserDTO userDTO;
    private static PasswordDTO passwordDTO;

    @BeforeEach
    public void setUpPerTest() {
        userDTO = new UserDTO("nameTest", "passwordTest0!", "passwordTest0!", "email@test.com");
    }

    @Test
    public void saveUserDoesNotExistShouldCreateNewUserTest() throws Exception {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn(userDTO.getPassword());

        userServiceTest.saveUser(userDTO);

        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(passwordEncoder, Mockito.times(1)).encode(userDTO.getPassword());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    public void saveUserAlreadyExistsShouldNotCreateNewUserTest() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> userServiceTest.saveUser(userDTO));
        assertEquals("There is already an account registered with the same email", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editPasswordShouldUpdateAttributePasswordUserTest() throws Exception {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        passwordDTO = new PasswordDTO("passwordTest0!", "newPasswordTest0!", "newPasswordTest0!");
        when(passwordEncoder.matches(passwordDTO.getOldPassword(),user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(passwordDTO.getNewPassword())).thenReturn(passwordDTO.getNewPassword());

        userServiceTest.editPassword(passwordDTO, user);

        verify(passwordEncoder, Mockito.times(1)).matches(passwordDTO.getOldPassword(),"passwordTest0!");
        verify(passwordEncoder, Mockito.times(1)).encode(passwordDTO.getNewPassword());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
        assertEquals("newPasswordTest0!",user.getPassword());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editPasswordWithoutSameOldNewPasswordShouldNotUpdateAttributePasswordUserTest() {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        passwordDTO = new PasswordDTO("wrongPasswordTest0!", "newPasswordTest0!", "newPasswordTest0!");
        when(passwordEncoder.matches(passwordDTO.getOldPassword(),user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(OldPasswordException.class, () -> userServiceTest.editPassword(passwordDTO, user));
        assertEquals("The old password doesn't match with the registered password", exception.getMessage());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editPasswordWithoutSameNewMatchingPasswordShouldNotUpdateAttributePasswordUserTest() {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        passwordDTO = new PasswordDTO("passwordTest0!", "newPasswordTest0!", "wrongNewPasswordTest0!");

        Exception exception = assertThrows(MatchingPasswordException.class, () -> userServiceTest.editPassword(passwordDTO, user));
        assertEquals("The matching password doesn't match with the password", exception.getMessage());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void editNameShouldUpdateAttributeNameUserTest() {
        user = new User("email@test.com",0f,"existingUser","passwordTest0!",new ArrayList<>());
        assertEquals("existingUser",user.getName());

        userServiceTest.editName("nameTest", user);

        assertEquals("nameTest",user.getName());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void addConnectionShouldUpdateAttributeConnectionsUserTest() throws UserDoesntExistException, AlreadyExistingConnection {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        List<User> expectedConnections = new ArrayList<>(List.of(user2));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        userServiceTest.addConnection(user.getEmail(),user2.getEmail());

        assertEquals(expectedConnections,user.getConnections());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void addConnectionWithNotExistingUser1ShouldNotUpdateAttributeConnectionsUserTest() {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        List<User> expectedConnections = new ArrayList<>();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(null);

        Exception exception = assertThrows(UserDoesntExistException.class, () ->
                userServiceTest.addConnection(user.getEmail(),user2.getEmail()));
        assertEquals("There is no account registered with this email", exception.getMessage());
        assertEquals(expectedConnections,user.getConnections());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void addConnectionWithNotExistingUser2ShouldNotUpdateAttributeConnectionsUserTest() {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        List<User> expectedConnections = new ArrayList<>();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(UserDoesntExistException.class, () ->
                userServiceTest.addConnection(user.getEmail(),user2.getEmail()));
        assertEquals("There is no account registered with this email", exception.getMessage());
        assertEquals(expectedConnections,user.getConnections());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void addConnectionWithExistingConnectionUser2ShouldNotUpdateAttributeConnectionsUserTest() {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>(List.of(user)));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(AlreadyExistingConnection.class, () ->
                userServiceTest.addConnection(user.getEmail(),user2.getEmail()));
        assertEquals("The connection already exists between these two users", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void addConnectionWithExistingConnectionUser1ShouldNotUpdateAttributeConnectionsUserTest() {
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>(List.of(user2)));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(AlreadyExistingConnection.class, () ->
                userServiceTest.addConnection(user.getEmail(),user2.getEmail()));
        assertEquals("The connection already exists between these two users", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void addConnectionWithSameUserShouldNotUpdateAttributeConnectionsUserTest() {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>(List.of(user)));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(AlreadyExistingConnection.class, () ->
                userServiceTest.addConnection(user.getEmail(),user2.getEmail()));
        assertEquals("The connection already exists between these two users", exception.getMessage());
        verify(userRepository, Mockito.times(2)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void deleteConnectionUser2ShouldUpdateAttributeConnectionsUserTest() throws NotExistingConnection {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>(List.of(user)));
        List<User> expectedConnections = new ArrayList<>();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        userServiceTest.deleteConnection(user.getEmail(),user2.getEmail());

        assertEquals(expectedConnections,user.getConnections());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void deleteConnectionUser1ShouldUpdateAttributeConnectionsUserTest() throws NotExistingConnection {
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>(List.of(user2)));
        List<User> expectedConnections = new ArrayList<>();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        userServiceTest.deleteConnection(user.getEmail(),user2.getEmail());

        assertEquals(expectedConnections,user.getConnections());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void deleteConnectionWithNotExistingConnectionShouldNotUpdateAttributeConnectionsUserTest() {
        user = new User("email@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        User user2 = new User("email2@test.com",100f,"existingUser","passwordTest0!",new ArrayList<>());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);

        Exception exception = assertThrows(NotExistingConnection.class, () ->
                userServiceTest.deleteConnection(user.getEmail(),user2.getEmail()));
        assertEquals("The connection doesn't exist between these two users", exception.getMessage());
        verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getConnectionUser1ShouldShowPagesWithAllConnectionsUserTest() {
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        UserDTO user2Dto = userServiceTest.mapToUserDto(user2);
        userDTO.getConnections().add(user2);
        when(userRepository.findAllConnectionsByEmail(userDTO.getEmail(), PageRequest.of(0, 3))).thenReturn(new PageImpl<>(List.of("email2@test.com")));
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        List<UserDTO> expectedResult = new ArrayList<>(List.of(user2Dto));

        Page<UserDTO> result = userServiceTest.getConnections(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(1,result.getTotalElements());
        assertEquals(expectedResult, result.getContent());
        verify(userRepository, Mockito.times(1)).findAllConnectionsByEmail(userDTO.getEmail(), PageRequest.of(0, 3));
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getConnectionUser2ShouldShowPagesWithAllConnectionsUserTest() {
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        UserDTO user2Dto = userServiceTest.mapToUserDto(user2);
        user2Dto.getConnections().add(user);
        when(userRepository.findAllConnectionsByEmail(userDTO.getEmail(), PageRequest.of(0, 3))).thenReturn(new PageImpl<>(List.of("email2@test.com")));
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        List<UserDTO> expectedResult = new ArrayList<>(List.of(user2Dto));

        Page<UserDTO> result = userServiceTest.getConnections(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(1,result.getTotalElements());
        assertEquals(expectedResult, result.getContent());
        verify(userRepository, Mockito.times(1)).findAllConnectionsByEmail(userDTO.getEmail(), PageRequest.of(0, 3));
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getConnectionWithoutConnectionsShouldShowAnEmptyListTest() {
        when(userRepository.findAllConnectionsByEmail(userDTO.getEmail(), PageRequest.of(0, 3))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<UserDTO> result = userServiceTest.getConnections(userDTO.getEmail(), PageRequest.of(0, 3));

        assertEquals(0,result.getTotalElements());
        verify(userRepository, Mockito.times(1)).findAllConnectionsByEmail(userDTO.getEmail(), PageRequest.of(0, 3));
        verify(userRepository, Mockito.never()).findByEmail(userDTO.getEmail());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    public void getConnectionUser1ShouldShowListWithAllConnectionsUserTest() {
        User user2 = new User("email2@test.com",100f,"existingUser2","passwordTest0!",new ArrayList<>());
        UserDTO user2Dto = userServiceTest.mapToUserDto(user2);
        userDTO.getConnections().add(user2);
        when(userRepository.findAllConnectionsByEmail(userDTO.getEmail())).thenReturn(List.of("email2@test.com"));
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        List<UserDTO> expectedResult = new ArrayList<>(List.of(user2Dto));

        List<UserDTO> result = userServiceTest.getConnections(userDTO);

        assertEquals(1,result.size());
        assertEquals(expectedResult, result);
        verify(userRepository, Mockito.times(1)).findAllConnectionsByEmail(userDTO.getEmail());
        verify(userRepository, Mockito.times(1)).findByEmail(user2.getEmail());
    }

}
