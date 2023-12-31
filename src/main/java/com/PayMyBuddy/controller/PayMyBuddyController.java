package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.security.AuthenticatedUserProvider;
import com.PayMyBuddy.service.TransactionService;
import com.PayMyBuddy.service.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Management of the POST requests though the controller.
 */
@Controller
public class PayMyBuddyController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthenticatedUserProvider authenticatedUserProvider;

    /**
     * Registration Post endpoint to register a new user
     *
     * @param userDto       the user dto to create a user
     * @param bindingResult the binding result in case of invalid parameters
     * @param model         the model
     * @return the view "home" in case of success and "register" in the opposite
     */
    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") UserDTO userDto, BindingResult bindingResult, Model model) {
        logger.info("request the creating of the user {}", userDto.getName());
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.saveUser(userDto);
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    /**
     * Transfer Post endpoint to make a transaction between two users.
     *
     * @param amount the amount to send
     * @param connections the email 1 to send money to
     * @param model  the model
     * @return the view "transfer" with a success or error message
     */
    @PostMapping("/transfer")
    public String sendMoneyFromTransfer(float amount, String connections, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the money transfer about {}€ between the users {} and {}", amount, connections, currentUser.getEmail());
        try {
            transactionService.sendMoney(amount, ("transaction " + LocalDate.now()), connections, currentUser.getEmail());
            return "redirect:/transfer?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<User> connectionsUser = userService.getConnections(currentUser.getEmail());
            List<UserDTO> connectionsDTO = new ArrayList<>();
            connectionsUser.forEach(connection -> connectionsDTO.add(userService.mapToUserDto(connection)));
            model.addAttribute("connectionsUser", connectionsDTO);
            final Page<TransactionDTO> pageTransactions = transactionService.getTransactions(
                    currentUser.getEmail(),
                    PageRequest.of(0, 3, Sort
                            .by(Sort.Direction.DESC,"date")
                            .and(Sort.by(Sort.Direction.ASC,"amount"))));
            model.addAttribute("transactions", pageTransactions);
            int totalPages = pageTransactions.getTotalPages();
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
            return "transfer";
        }

    }

    /**
     * Add connection Post endpoint to add a new contact.
     *
     * @param email the email to add
     * @param model the model
     * @return the view "contact" in case of success and "addConnection" in the opposite
     */
    @PostMapping("/addConnection")
    public String addConnection(String email, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request a connection between the users {} and {}", currentUser.getName(), email);
        try {
            userService.addConnection(currentUser.getEmail(), email);
            return "redirect:/contact?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "addConnection";
        }
    }

    /**
     * Delete connection Post endpoint to remove a contact.
     *
     * @param connectionEmail the email to remove
     * @param model  the model
     * @return the view "contact" in case of success and "deleteConnection" in the opposite
     */
    @PostMapping("/deleteConnection")
    public String deleteConnection(String connectionEmail, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the connection delete between the users {} and {}", connectionEmail, currentUser.getEmail());
        try {
            userService.deleteConnection(currentUser.getEmail(), connectionEmail);
            return "redirect:/contact?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            UserDTO user = userService.mapToUserDto(currentUser);
            model.addAttribute("user", user);
            return "error";
        }
    }

    /**
     * Edit name Post endpoint to update the user's name.
     *
     * @param nameUser      the new name
     * @param bindingResult the binding result
     * @param model the model
     * @return the view "profile" in case of success and "editName" in the opposite
     */
    @PostMapping("/editName")
    public String editName(@Valid @ModelAttribute("user") UserDTO nameUser, BindingResult bindingResult, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the name's update of the user {}", currentUser.getName());
        if (bindingResult.hasErrors()) {
            return "editName";
        }
        try {
            userService.editName(nameUser.getName(), currentUser);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            UserDTO user = userService.mapToUserDto(currentUser);
            model.addAttribute("user", user);
            return "editName";
        }
    }


    /**
     * Edit password Post endpoint to update the user's password.
     *
     * @param passwordDTO   the password dto containing the old and new one
     * @param bindingResult the binding result
     * @param model         the model
     * @return the view "logout" in case of success and "editPassword" in the opposite
     */
    @PostMapping("/editPassword")
    public String editPassword(@Valid @ModelAttribute("password") PasswordDTO passwordDTO, BindingResult bindingResult, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the password's update of the user {}", currentUser.getName());

        if (bindingResult.hasErrors()) {
            return "editPassword";
        }
        try {
            userService.editPassword(passwordDTO, currentUser);
            return "redirect:/logout";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "editPassword";
        }
    }


    /**
     * Deposit Post endpoint to transfer money from the user's bank.
     *
     * @param amount the amount to transfer
     * @param model  the model
     * @return the view "profile" in case of success and "deposit" in the opposite
     */
    @PostMapping("/deposit")
    public String deposit(float amount, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the deposit by the user {}", currentUser.getName());
        try {
            transactionService.deposit(amount, currentUser);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            UserDTO user = userService.mapToUserDto(currentUser);
            model.addAttribute("user", user);
            return "deposit";
        }
    }


    /**
     * Withdraw Post endpoint to transfer money to the user's bank.
     *
     * @param amount the amount to transfer
     * @param model the model
     * @return the view "profile" in case of success and "withdraw" in the opposite
     */
    @PostMapping("/withdraw")
    public String withdraw(float amount, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the withdraw by the user {}", currentUser.getName());
        try {
            transactionService.withdraw(amount, currentUser);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            UserDTO user = userService.mapToUserDto(currentUser);
            model.addAttribute("user", user);
            return "withdraw";
        }
    }

    /**
     * send money Post endpoint .
     *
     * @param amount      the amount to transfer
     * @param description the description about the transfer
     * @param email      the email to transfer
     * @param model       the model
     * @return the view "transfer" in case of success and "sendMoney" in the opposite
     */
    @PostMapping("/sendMoney")
    public String sendMoneyFromContact(float amount, String description, String email, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the money transfer about {}€ between the users {} and {}", amount, email, currentUser.getEmail());
        try {
            transactionService.sendMoney(amount, description, email, currentUser.getEmail());
            return "redirect:/transfer?success";
        } catch (Exception e) {
            User connectionUser = userService.loadUserByUsername(email);
            UserDTO connectionUserDto = userService.mapToUserDto(connectionUser);
            model.addAttribute("receiverUser", connectionUserDto);
            UserDTO user = userService.mapToUserDto(currentUser);
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());
            return "sendMoney";
        }
    }
}
