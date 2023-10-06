package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.service.TransactionService;
import com.PayMyBuddy.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class PayMyBuddyControllerIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.26"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort())
            .withEnv("MYSQL_ROOT_HOST", "%");

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,  "connections", "transactions", "users");
    }

    @BeforeEach
    void setUpTest() throws Exception {
        UserDTO existingUser = new UserDTO("existingUserNameTest","passwordTest!0","passwordTest!0","existingUserTest@email.test", 0f, new ArrayList<>());
        userService.saveUser(existingUser);

        UserDTO existingUser2 = new UserDTO("existingUser2NameTest","passwordTest!0","passwordTest!0","existingUser2Test@email.test", 0f, new ArrayList<>());
        userService.saveUser(existingUser2);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saveUserShouldCreateNewUserTest() throws Exception {
        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));

        assertNotNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithNameWithSpacesShouldCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","name Test")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));

        assertNotNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithCompoundNameShouldCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","name-Test")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));

        assertNotNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveExistingUserShouldNotCreateNewUserTest() throws Exception {
        assertNotNull((userService.loadUserByUsername("existingUserTest@email.test")));

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","existingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));
    }

    @Test
    void saveUserWithNotSamePasswordsShouldNotCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","notSamePasswordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));

        assertNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithNameWithOnlySpacesShouldNotCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","  ")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
;

        assertNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithNameWithOnlyHyphensShouldNotCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","--")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));

        assertNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithNullsParameterShouldNotCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", (String) null)
                        .param("password", (String) null)
                        .param("matchingPassword", (String) null)
                        .param("email", "userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));

        assertNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithoutParametersShouldNotCreateNewUserTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));

        assertNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameShouldUpdateAttributeNameUserTest() throws Exception {
        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","updateNameTest")
                        .param("email","existingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals("updateNameTest",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithNameWithSpacesShouldUpdateAttributeNameUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","update name Test")
                        .param("email","existingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals("update name Test",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithCompoundNameShouldUpdateAttributeNameUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","name-Test")
                        .param("email","existingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals("name-Test",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithoutParametersShouldNotUpdateAttributeNameUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editName"));

        assertNotEquals("updateNameTest",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithNameWithOnlySpacesShouldNotUpdateAttributeNameUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","  ")
                        .with(csrf()))
        ;

        assertNotEquals("  ",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithNameWithOnlyHyphensShouldNotUpdateAttributeNameUserTest() throws Exception {
        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","--")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editName"));

        assertNotEquals("--",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithNullsParameterShouldNotUpdateAttributeNameUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", (String) null)
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editName"));

        assertNotEquals(null,userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editPasswordShouldUpdateAttributePasswordUserTest() throws Exception {
        this.mockMvc
                .perform(post("/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("oldPassword","passwordTest!0")
                        .param("newPassword","newPasswordTest!0")
                        .param("matchingPassword","newPasswordTest!0")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"));

        assertTrue(passwordEncoder.matches("newPasswordTest!0",userService.loadUserByUsername("existingUserTest@email.test").getPassword()));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editPasswordWithNullsParameterShouldNotUpdateAttributePasswordUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("oldPassword", (String) null)
                        .param("newPassword","newPasswordTest!0")
                        .param("matchingPassword","newPasswordTest!0")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editPassword"));

        assertFalse(passwordEncoder.matches("newPasswordTest!0",userService.loadUserByUsername("existingUserTest@email.test").getPassword()));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editPasswordWithoutSameOldNewPasswordShouldNotUpdateAttributePasswordUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("oldPassword","wrongPasswordTest!0")
                        .param("newPassword","newPasswordTest!0")
                        .param("matchingPassword","newPasswordTest!0")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editPassword"));

        assertFalse(passwordEncoder.matches("newPasswordTest!0",userService.loadUserByUsername("existingUserTest@email.test").getPassword()));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editPasswordWithInvalidValueShouldNotUpdateAttributePasswordUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("oldPassword","passwordTest!0")
                        .param("newPassword","invalidPasswordTest")
                        .param("matchingPassword","invalidPasswordTest")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editPassword"));

        assertFalse(passwordEncoder.matches("newPasswordTest!0",userService.loadUserByUsername("existingUserTest@email.test").getPassword()));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editPasswordWithoutSameNewMatchingPasswordShouldNotUpdateAttributePasswordUserTest() throws Exception {

        this.mockMvc
                .perform(post("/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("oldPassword","passwordTest!0")
                        .param("newPassword","newPasswordTest!0")
                        .param("matchingPassword","wrongNewPasswordTest!0")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editPassword"));

        assertFalse(passwordEncoder.matches("newPasswordTest!0",userService.loadUserByUsername("existingUserTest@email.test").getPassword()));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void deleteConnectionShouldUpdateAttributeConnectionsUserTest() throws Exception {
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setConnections(List.of(existingUser2));
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/deleteConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("connectionEmail","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact?success"))
                .andExpect(view().name("redirect:/contact?success"));

        assertEquals(0, userRepository.findByEmail("existingUserTest@email.test").getConnections().size());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void deleteConnectionWithoutConnectionsShouldNotUpdateAttributeConnectionsUserTest() throws Exception {
        this.mockMvc
                .perform(post("/deleteConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("connectionEmail","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void addConnectionShouldUpdateAttributeConnectionsUserTest() throws Exception {
        this.mockMvc
                .perform(post("/addConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/contact?success"));
        assertEquals("existingUser2Test@email.test", userRepository.findByEmail("existingUserTest@email.test").getConnections().get(0).getEmail());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void addConnectionWithInvalidUserShouldNotUpdateAttributeConnectionsUserTest() throws Exception {
        this.mockMvc
                .perform(post("/addConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email","existingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("addConnection"));
        assertEquals(0, userRepository.findByEmail("existingUserTest@email.test").getConnections().size());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void addConnectionWithNotExistingUserShouldNotUpdateAttributeConnectionsUserTest() throws Exception {
        this.mockMvc
                .perform(post("/addConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email","NotExistingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("addConnection"));
        assertEquals(0, userRepository.findByEmail("existingUserTest@email.test").getConnections().size());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void depositShouldUpdateAttributeBalanceUserTest() throws Exception {
        this.mockMvc
                .perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals(1,userService.loadUserByUsername("existingUserTest@email.test").getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void depositWithNegativeAmountShouldNotUpdateAttributeBalanceUserTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(1f);
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/deposit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","-1")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("deposit"));

        assertEquals(1f,userRepository.findByEmail("existingUserTest@email.test").getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void withdrawShouldUpdateAttributeBalanceUserTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(1f);
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals(0,userRepository.findByEmail("existingUserTest@email.test").getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void withdrawWithNegativeAmountShouldNotUpdateAttributeBalanceUserTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(1f);
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","-1")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("withdraw"));

        assertEquals(1f,userRepository.findByEmail("existingUserTest@email.test").getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void withdrawWithAmountGreaterThanBalanceShouldNotUpdateAttributeBalanceUserTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(1f);
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","2")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("withdraw"));

        assertEquals(1f,userRepository.findByEmail("existingUserTest@email.test").getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void postSendMoneyShouldUpdateAttributeBalanceBothUsersAndCreateNewTransactionTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser.setBalance(10.05f);
        existingUser.getConnections().add(existingUser2);
        userRepository.save(existingUser);
        this.mockMvc
                .perform(post("/sendMoney")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","10f")
                        .param("description","transactionTest")
                        .param("email","existingUser2Test@email.test")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/transfer?success"));

        assertEquals(1,transactionRepository.findBySenderUser(existingUser).size());
        assertEquals(0,userRepository.findByEmail(existingUser.getEmail()).getBalance());
        assertEquals(10f,userRepository.findByEmail(existingUser2.getEmail()).getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void postSendMoneyWithInvalidAmountShouldNotUpdateAttributeBalanceBothUsersAndNotCreateNewTransactionTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser.getConnections().add(existingUser2);
        userRepository.save(existingUser);
        this.mockMvc
                .perform(post("/sendMoney")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","10f")
                        .param("description","transactionTest")
                        .param("email","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("sendMoney"));

        assertEquals(0,transactionRepository.findBySenderUser(userRepository.findByEmail("existingUserTest@email.test")).size());
        assertEquals(0,userRepository.findByEmail(existingUser.getEmail()).getBalance());
        assertEquals(0,userRepository.findByEmail(existingUser2.getEmail()).getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void postSendMoneyWithInvalidUserShouldNotUpdateAttributeBalanceBothUsersAndNotCreateNewTransactionTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        this.mockMvc
                .perform(post("/sendMoney")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount","10f")
                        .param("description","transactionTest")
                        .param("email","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("sendMoney"));

        assertEquals(0,transactionRepository.findBySenderUser(userRepository.findByEmail("existingUserTest@email.test")).size());
        assertEquals(0,userRepository.findByEmail(existingUser.getEmail()).getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void sendMoneyShouldUpdateAttributeBalanceBothUsersAndCreateNewTransactionTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser.setBalance(10.05f);
        existingUser.getConnections().add(existingUser2);
        userRepository.save(existingUser);
        userRepository.save(existingUser2);
        this.mockMvc
                .perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "10.00")
                        .param("connections","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/transfer?success"));

        assertEquals(1,transactionRepository.findBySenderUser(userRepository.findByEmail("existingUserTest@email.test")).size());
        assertEquals(0,userRepository.findByEmail(existingUser.getEmail()).getBalance());
        assertEquals(10,userRepository.findByEmail(existingUser2.getEmail()).getBalance());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void sendMoneyWithInvalidAmountShouldNotUpdateAttributeBalanceBothUsersAndNotCreateNewTransactionTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser.setBalance(10f);
        existingUser.getConnections().add(existingUser2);
        userRepository.save(existingUser);
        userRepository.save(existingUser2);
        this.mockMvc
                .perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "-10.00")
                        .param("connections","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("transfer"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void sendMoneyWithInvalidUserShouldNotUpdateAttributeBalanceBothUsersAndNotCreateNewTransactionTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(10f);
        userRepository.save(existingUser);
        this.mockMvc
                .perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "10.00")
                        .param("connections","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("transfer"));
    }

}
