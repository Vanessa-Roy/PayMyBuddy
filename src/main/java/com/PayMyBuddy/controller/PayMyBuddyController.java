package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.Transaction;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class PayMyBuddyController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        logger.info("request the form page");
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registration(@Valid @ModelAttribute("user") UserDTO userDto, BindingResult bindingResult, Model model) {
        logger.info("request the creating of the user {}", userDto.getName());
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.saveUser(userDto);
            return "redirect:/register?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/users")
    public String users(Model model){
        logger.info("request the users page");
        List<UserDTO> users = userService.findAllUsers();
        model.addAttribute("user", users);
        return "users";
    }

    @GetMapping("/login")
    public String login(){
        logger.info("request the login page");
        return "login";
    }

    @GetMapping("/transfer")
    public String transfer(Model model, Optional<Integer> page, Optional<Integer> size){

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(3);
        logger.info("request the transfer page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        List<TransactionDTO> transactions = userService.getTransaction(user);
        final Page<TransactionDTO> pageTransactions = userService.getPaginatedTransactions(PageRequest.of(currentPage - 1, pageSize), transactions);
        model.addAttribute("transactions", pageTransactions);

        int totalPages = pageTransactions.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "transfer";
    }

    @GetMapping("/contact")
    public String contact(Model model, Optional<Integer> page, Optional<Integer> size){

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(3);
        logger.info("request the contact page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        List<UserDTO> connections = userService.getConnection(user);
        final Page<UserDTO> pageConnections = userService.getPaginatedConnection(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Order.asc("name"))), connections);
        model.addAttribute("connections", pageConnections);

        int totalPages = pageConnections.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "contact";
    }

    @GetMapping("/addConnection")
    public String addConnection(Model model){
        logger.info("request the add connection page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "addConnection";
    }

    @PostMapping("/addConnection")
    public String addConnection(@ModelAttribute("email") String email, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        logger.info("request a connection between the users {} and {}", user.getName(), email);
        try {
            userService.addConnection(user.getEmail(), email);
            return "redirect:/connections?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "addConnection";
        }
    }

    @GetMapping("/deleteConnection")
    public String getDeleteConnection(String email, Model model){
        logger.info("request the delete connection page");
        User connectionUser = userService.loadUserByUsername(email);
        UserDTO user = userService.mapToUserDto(connectionUser);
        model.addAttribute("user", user);
        return "deleteConnection";
    }

    @PostMapping("/deleteConnection")
    public String postDeleteConnection(String email1, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        logger.info("request the connection delete between the users {} and {}", email1, user.getEmail());
        try {
            userService.deleteConnection(email1, user.getEmail());
            return "redirect:/connections?success";
        } catch (Exception e) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());
            return "deleteConnection";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model){
        logger.info("request the profile page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/editName")
    public String editName(Model model){
        logger.info("request the editName page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "editName";
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

    @GetMapping("/editPassword")
    public String editPassword(Model model){
        logger.info("request the editPassword page");
        PasswordDTO password = new PasswordDTO();
        model.addAttribute("password", password);
        return "editPassword";
    }

    @PostMapping("/editPassword")
    public String editPassword(@Valid @ModelAttribute("password") PasswordDTO passwordDTO, BindingResult bindingResult, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("request the password's update of the user {}", auth.getName());

        if (bindingResult.hasErrors()) {
            return "editPassword";
        }
        try {
            userService.editPassword(passwordDTO);
            return "redirect:/logout";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "editPassword";
        }
    }

    @GetMapping("/deposit")
    public String deposit(Model model){
        logger.info("request the deposit page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@ModelAttribute("amount") float amount, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        logger.info("request the deposit by the user {}", user.getName());
        try {
            userService.deposit(amount);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "deposit";
        }
    }

    @GetMapping("/withdraw")
    public String withdraw(Model model){
        logger.info("request the withdraw page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@ModelAttribute("amount") float amount, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        logger.info("request the withdraw by the user {}", user.getName());
        try {
            userService.withdraw(amount);
            return "redirect:/profile?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "withdraw";
        }
    }

    @GetMapping("/sendMoney")
    public String getSendMoney(String email, Model model){
        logger.info("request the send money page");
        User connectionUser = userService.loadUserByUsername(email);
        UserDTO connectionUserDto = userService.mapToUserDto(connectionUser);
        model.addAttribute("receiverUser", connectionUserDto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "sendMoney";
    }

    @PostMapping("/sendMoney")
    public String postSendMoney(@ModelAttribute("amount") float amount, @ModelAttribute("description") String description, String email1, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        logger.info("request the money transfer about {}$ between the users {} and {}", amount, email1, user.getEmail());
        try {
            userService.sendMoney(amount, description, email1, user.getEmail());
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
