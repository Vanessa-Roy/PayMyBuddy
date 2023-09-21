package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.TransactionService;
import com.PayMyBuddy.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class PayMyBuddyPageViewController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

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
        List<TransactionDTO> transactions = transactionService.getTransaction(user);
        List<UserDTO> connections = userService.getConnection(user);
        model.addAttribute("connections", connections);
        TransactionDTO transactionDTO = new TransactionDTO();
        model.addAttribute("transaction", transactionDTO);
        final Page<TransactionDTO> pageTransactions = transactionService.getPaginatedTransactions(PageRequest.of(currentPage - 1, pageSize), transactions);
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

    @GetMapping("/deleteConnection")
    public String getDeleteConnection(String email, Model model){
        logger.info("request the delete connection page");
        User connectionUser = userService.loadUserByUsername(email);
        UserDTO user = userService.mapToUserDto(connectionUser);
        model.addAttribute("user", user);
        return "deleteConnection";
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

    @GetMapping("/editPassword")
    public String editPassword(Model model){
        logger.info("request the editPassword page");
        PasswordDTO password = new PasswordDTO();
        model.addAttribute("password", password);
        return "editPassword";
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

    @GetMapping("/withdraw")
    public String withdraw(Model model){
        logger.info("request the withdraw page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "withdraw";
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
}
