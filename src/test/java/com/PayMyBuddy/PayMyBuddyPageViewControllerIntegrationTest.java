package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.Transaction;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class PayMyBuddyPageViewControllerIntegrationTest {

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
    void shouldAllowAccessToLoginForAnonymousUserTest() throws Exception {
        this.mockMvc
                .perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void shouldAllowAccessToRegisterForAnonymousUserTest() throws Exception {
        this.mockMvc
                .perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToHomeForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("name"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToProfileForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToEditNameForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/editName"))
                .andExpect(status().isOk())
                .andExpect(view().name("editName"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToEditPasswordForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/editPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("editPassword"))
                .andExpect(model().attributeExists("password"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToDepositForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/deposit"))
                .andExpect(status().isOk())
                .andExpect(view().name("deposit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToWithdrawForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/withdraw"))
                .andExpect(status().isOk())
                .andExpect(view().name("withdraw"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToAddConnectionForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/addConnection"))
                .andExpect(status().isOk())
                .andExpect(view().name("addConnection"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToDeleteConnectionForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/deleteConnection")
                        .param("email","existingUserTest@email.test"))
                .andExpect(status().isOk())
                .andExpect(view().name("deleteConnection"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToTransferForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/transfer"))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("transactions"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToSendMoneyForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/sendMoney")
                        .param("email","existingUserTest@email.test"))
                .andExpect(status().isOk())
                .andExpect(view().name("sendMoney"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void shouldDenyAccessToEditNameForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/editName"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToEditPasswordForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/editPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToHomeForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToProfileForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToDepositForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/deposit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToWithdrawPasswordForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/withdraw"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToAddConnectionForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/addConnection"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToDeleteConnectionForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/deleteConnection"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToConnectionForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/connections"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToTransferForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToContactForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/contact"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldDenyAccessToSendMoneyForUnauthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/sendMoney"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetConnectionWithPagesTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser2.getConnections().add((existingUser));
        userRepository.save(existingUser2);
        User existingUser3 = new User("existingUser3Test@email.test",0f,"existingUser3NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser3);
        User existingUser4 = new User("existingUser4Test@email.test",0f,"existingUser4NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser4);
        User existingUser5 = new User("existingUser5Test@email.test",0f,"existingUser5NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser5);

        this.mockMvc
                .perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("pageNumbers"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetTransactionWithPagesTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(100f);
        userRepository.save(existingUser);
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser2.getConnections().add((existingUser));
        userRepository.save(existingUser2);
        Transaction transaction1 = new Transaction(1, LocalDate.now(),"transactionTest1",10f,0.05f,existingUser,existingUser2);
        transactionRepository.save(transaction1);
        Transaction transaction2 = new Transaction(2, LocalDate.now(),"transactionTest2",10f,0.05f,existingUser,existingUser2);
        transactionRepository.save(transaction2);
        Transaction transaction3 = new Transaction(3, LocalDate.now(),"transactionTest3",10f,0.05f,existingUser,existingUser2);
        transactionRepository.save(transaction3);
        Transaction transaction4 = new Transaction(4, LocalDate.now(),"transactionTest4",10f,0.05f,existingUser,existingUser2);
        transactionRepository.save(transaction4);

        this.mockMvc
                .perform(get("/transfer"))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("pageNumbers"));
    }
    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetConnectionWithPagesWhoDoesNotExistTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser2.getConnections().add((existingUser));
        userRepository.save(existingUser2);

        this.mockMvc
                .perform(get("/contact?page=5"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("pageNumbers"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetTransactionWithPagesWhoDoesNotExistTest() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setBalance(100f);
        userRepository.save(existingUser);
        User existingUser2 = userRepository.findByEmail("existingUser2Test@email.test");
        existingUser2.getConnections().add((existingUser));
        userRepository.save(existingUser2);

        this.mockMvc
                .perform(get("/transfer?page=5"))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("pageNumbers"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetConnectionWithNoConnectionTest() throws Exception {

        this.mockMvc
                .perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("pageNumbers"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetTransactionWithNoTransactionTest() throws Exception {

        this.mockMvc
                .perform(get("/transfer"))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("pageNumbers"));
    }
}
