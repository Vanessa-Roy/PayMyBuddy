package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private UserRepository userRepository;

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
        UserDTO existingUser = new UserDTO("existingUserNameTest","passwordTest!0","passwordTest!0","existingUserTest@email.test");
        userService.saveUser(existingUser);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saveUserShouldPassTest() throws Exception {
        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/register?success"));

        assertNotNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithNameWithSpacesShouldPassTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","name Test")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/register?success"));

        assertNotNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveUserWithCompoundNameShouldPassTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","name-Test")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/register?success"));

        assertNotNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    void saveExistingUserShouldFailTest() throws Exception {
        UserDTO existingUser = new UserDTO("nameTest","passwordTest!0","passwordTest!0","userTest@email.com");
        userService.saveUser(existingUser);
        assertNotNull((userService.loadUserByUsername("userTest@email.com")));

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));
    }

    @Test
    void saveUserWithNotSamePasswordsShouldFailTest() throws Exception {

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
    void saveUserWithNameWithOnlySpacesShouldFailTest() throws Exception {

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
    void saveUserWithNameWithOnlyHyphensShouldFailTest() throws Exception {

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
    void saveUserWithNullsParameterShouldFailTest() throws Exception {

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
    void saveUserWithoutParametersShouldFailTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));

        assertNull((userService.loadUserByUsername("userTest@email.com")));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameShouldPassTest() throws Exception {
        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","updateNameTest")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals("updateNameTest",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithNameWithSpacesShouldPassTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","update name Test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals("update name Test",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithCompoundNameShouldPassTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","name-Test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?success"));

        assertEquals("name-Test",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithoutParametersShouldFailTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("editName"));

        assertNotEquals("updateNameTest",userService.loadUserByUsername("existingUserTest@email.test").getName());
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void editNameWithNameWithOnlySpacesShouldFailTest() throws Exception {

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
    void editNameWithNameWithOnlyHyphensShouldFailTest() throws Exception {

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
    void editNameWithNullsParameterShouldFailTest() throws Exception {

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
    void editPasswordShouldPassTest() throws Exception {
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
    void editPasswordWithNullsParameterShouldFailTest() throws Exception {

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
    void editPasswordWithoutSameOldNewPasswordShouldFailTest() throws Exception {

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
    void editPasswordWithInvalidValueShouldFailTest() throws Exception {

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
    void editPasswordWithoutSameNewMatchingPasswordShouldFailTest() throws Exception {

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
    void deleteConnectionShouldPassTest() throws Exception {
        User existingUser2 = new User("existingUser2Test@email.test",0f,"existingUser2NameTest","passwordTest!0",new ArrayList<>(List.of()));
        userRepository.save(existingUser2);
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        existingUser.setConnections(List.of(existingUser2));
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/deleteConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email1","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connections?success"))
                .andExpect(view().name("redirect:/connections?success"));

        assertTrue(userRepository.findByEmail("existingUserTest@email.test").getConnections().size()==0);
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void deleteConnectionWithoutConnectionsShouldFailTest() throws Exception {
        User existingUser2 = new User("existingUser2Test@email.test",0f,"existingUser2NameTest","passwordTest!0",new ArrayList<>(List.of()));
        userRepository.save(existingUser2);
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        userRepository.save(existingUser);

        this.mockMvc
                .perform(post("/deleteConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email1","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("deleteConnection"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void addConnectionShouldPassTest() throws Exception {
        User existingUser2 = new User("existingUser2Test@email.test",0f,"existingUser2NameTest","passwordTest!0",new ArrayList<>());
        userRepository.save(existingUser2);
        this.mockMvc
                .perform(post("/addConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email","existingUser2Test@email.test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/connections?success"));
        assertTrue(userRepository.findByEmail("existingUserTest@email.test").getConnections().get(0).getEmail().equals("existingUser2Test@email.test"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void addConnectionWithInvalidUserShouldFailTest() throws Exception {
        this.mockMvc
                .perform(post("/addConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email","existingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("addConnection"));
        assertTrue(userRepository.findByEmail("existingUserTest@email.test").getConnections().size()==0);
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void addConnectionWithNotExistingUserShouldFailTest() throws Exception {
        this.mockMvc
                .perform(post("/addConnection")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email","NotExistingUserTest@email.test")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("addConnection"));
        assertTrue(userRepository.findByEmail("existingUserTest@email.test").getConnections().size()==0);
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void depositShouldPassTest() throws Exception {
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
    void depositWithNegativeAmountShouldFailTest() throws Exception {
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
    void withdrawShouldPassTest() throws Exception {
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
    void withdrawWithNegativeAmountShouldFailTest() throws Exception {
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
    void withdrawWithAmountGreaterThanBalanceShouldFailTest() throws Exception {
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
    void getConnectionWithPagesShouldPass() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = new User("existingUser2Test@email.test",0f,"existingUser2NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser2);
        User existingUser3 = new User("existingUser3Test@email.test",0f,"existingUser3NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser3);
        User existingUser4 = new User("existingUser4Test@email.test",0f,"existingUser4NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser4);
        User existingUser5 = new User("existingUser5Test@email.test",0f,"existingUser5NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser5);

        this.mockMvc
                .perform(get("/connections"))
                .andExpect(status().isOk())
                .andExpect(view().name("connections"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("pageNumbers"));
    }
    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void getConnectionWithPagesWhoDoesNotExistShouldPass() throws Exception {
        User existingUser = userRepository.findByEmail("existingUserTest@email.test");
        User existingUser2 = new User("existingUser2Test@email.test",0f,"existingUser2NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser2);
        User existingUser3 = new User("existingUser3Test@email.test",0f,"existingUser3NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser3);
        User existingUser4 = new User("existingUser4Test@email.test",0f,"existingUser4NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser4);
        User existingUser5 = new User("existingUser5Test@email.test",0f,"existingUser5NameTest","passwordTest!0",new ArrayList<>(List.of(existingUser)));
        userRepository.save(existingUser5);

        this.mockMvc
                .perform(get("/connections?page=5"))
                .andExpect(status().isOk())
                .andExpect(view().name("connections"))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("pageNumbers"));
    }

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
    @WithMockUser
    void shouldAllowAccessToHomeForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
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
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("amount"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToWithdrawForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/withdraw"))
                .andExpect(status().isOk())
                .andExpect(view().name("withdraw"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("amount"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToGetConnectionForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/connections"))
                .andExpect(status().isOk())
                .andExpect(view().name("connections"))
                .andExpect(model().attributeExists("connections"));
    }

    @Test
    @WithMockUser(username = "existingUserTest@email.test")
    void shouldAllowAccessToAddConnectionForAuthenticatedUserTest() throws Exception {
        this.mockMvc
                .perform(get("/addConnection"))
                .andExpect(status().isOk())
                .andExpect(view().name("addConnection"))
                .andExpect(model().attributeExists("user"));
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


}
