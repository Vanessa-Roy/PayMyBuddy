package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.security.AuthenticatedUserProvider;
import com.PayMyBuddy.service.TransactionService;
import com.PayMyBuddy.service.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class PayMyBuddyController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") UserDTO userDto, BindingResult bindingResult, Model model) {
        logger.info("request the creating of the user {}", userDto.getName());
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.saveUser(userDto);
            return "redirect:/home?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/transfer")
    public String sendMoney(@ModelAttribute("amount") float amount, @ModelAttribute("connections") String email1, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        logger.info("request the money transfer about {}$ between the users {} and {}", amount, email1, user.getEmail());
        try {
            transactionService.sendMoney(amount, ("transaction " + LocalDate.now().toString()), email1, user.getEmail());
            return "redirect:/transfer?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/addConnection")
    public String addConnection(@ModelAttribute("email") String email, Model model) {
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        logger.info("request a connection between the users {} and {}", user.getName(), email);
        try {
            userService.addConnection(user.getEmail(), email);
            return "redirect:/contact?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "addConnection";
        }
    }

    @PostMapping("/deleteConnection")
    public String postDeleteConnection(String email1, Model model) {
        User existingUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(existingUser);
        logger.info("request the connection delete between the users {} and {}", email1, user.getEmail());
        try {
            userService.deleteConnection(email1, user.getEmail());
            return "redirect:/contact?success";
        } catch (Exception e) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());
            return "deleteConnection";
        }
    }

    @PostMapping("/editName")
    public String editName(@Valid @ModelAttribute("user") UserDTO userDto, BindingResult bindingResult, Model model) {
        logger.info("request the name's update of the user {}", userDto.getName());
        if (bindingResult.hasErrors()) {
            return "editName";
        }
        try {
            userService.editName(userDto);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "editName";
        }
    }


    @PostMapping("/editPassword")
    public String editPassword(@Valid @ModelAttribute("password") PasswordDTO passwordDTO, BindingResult bindingResult, Model model) {
        User existingUser = authenticatedUserProvider.getAuthenticatedUser();
        logger.info("request the password's update of the user {}", existingUser.getName());

        if (bindingResult.hasErrors()) {
            return "editPassword";
        }
        try {
            userService.editPassword(passwordDTO, existingUser);
            return "redirect:/logout";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "editPassword";
        }
    }


    @PostMapping("/deposit")
    public String deposit(@ModelAttribute("amount") float amount, Model model) {
        User existingUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        logger.info("request the deposit by the user {}", user.getName());
        try {
            transactionService.deposit(amount, existingUser);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "deposit";
        }
    }


    @PostMapping("/withdraw")
    public String withdraw(@ModelAttribute("amount") float amount, Model model) {
        User existingUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        logger.info("request the withdraw by the user {}", user.getName());
        try {
            transactionService.withdraw(amount, existingUser);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "withdraw";
        }
    }

    @PostMapping("/sendMoney")
    public String postSendMoney(@ModelAttribute("amount") float amount, @ModelAttribute("description") String description, String email1, Model model) {
        User existingUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        logger.info("request the money transfer about {}$ between the users {} and {}", amount, email1, user.getEmail());
        try {
            transactionService.sendMoney(amount, description, email1, user.getEmail());
            return "redirect:/transfer?success";
        } catch (Exception e) {
            User connectionUser = userService.loadUserByUsername(email1);
            UserDTO connectionUserDto = userService.mapToUserDto(connectionUser);
            model.addAttribute("receiverUser", connectionUserDto);
            model.addAttribute("errorMessage", e.getMessage());
            return "sendMoney";
        }
    }
}
