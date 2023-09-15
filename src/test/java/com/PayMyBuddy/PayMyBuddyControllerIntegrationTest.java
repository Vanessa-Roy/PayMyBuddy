package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users", "transactions");
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
    void saveUserWithoutCsrfShouldFailTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest")
                        .param("password","passwordTest!0")
                        .param("matchingPassword","passwordTest!0")
                        .param("email","userTest@email.com"))
                .andExpect(status().is4xxClientError());

        assertNull((userService.loadUserByUsername("userTest@email.com")));
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
    void editNameWithoutCsrfShouldFailTest() throws Exception {

        this.mockMvc
                .perform(post("/editName")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name","nameTest"))
                .andExpect(status().is4xxClientError());

        assertNotEquals("updateNameTest",userService.loadUserByUsername("existingUserTest@email.test").getName());
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
    void editPasswordWithoutCsrfShouldFailTest() throws Exception {

        this.mockMvc
                .perform(post("/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("oldPassword","passwordTest!0")
                        .param("newPassword","newPasswordTest!0")
                        .param("matchingPassword","newPasswordTest!0"))
                .andExpect(status().is4xxClientError());

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


}
