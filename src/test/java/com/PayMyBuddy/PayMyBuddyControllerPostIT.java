package com.PayMyBuddy;

import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.repository.UserRepository;
import com.PayMyBuddy.service.UserService;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class PayMyBuddyControllerPostIT {

    @Autowired
    private DataSource dataSource;

    @Container
    static final MySQLContainer<?> mySQLContainer;

    static {
        mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.26"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .waitingFor(Wait.forListeningPort())
                .withEnv("MYSQL_ROOT_HOST", "%");
    }

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
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
    @Autowired
    private MockMvc mockMvc;

    @Test
    void saveUserShouldPassTest() throws Exception {
        UserDTO expectedUser = new UserDTO("nameTest","passwordTest!0","passwordTest!0","userTest@email.com");

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
    void saveUserWithoutParametersShouldFailTest() throws Exception {

        this.mockMvc
                .perform(post("/register")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("register"));
    }


}
