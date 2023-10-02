package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.PasswordDTO;
import com.PayMyBuddy.dto.TransactionDTO;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.repository.TransactionRepository;
import com.PayMyBuddy.security.AuthenticatedUserProvider;
import com.PayMyBuddy.security.CustomOAuth2User;
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

/**
 * Management of the view though the controller.
 */
@Controller
public class PayMyBuddyPageViewController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Home Get Get endpoint.
     *
     * @param model the model
     * @return the view "home" with the attribute "name"
     */
    @GetMapping("/home")
    public String home(Model model){
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        String name = currentUser.getName();
        model.addAttribute("name", name);
        return "home";
    }

    /**
     * register Get endpoint.
     *
     * @param model the model
     * @return the view "register" with the attribute "user"
     */
    @GetMapping("/register")
    public String register(Model model){
        logger.info("request the form page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            UserDTO userDTO = new UserDTO(oAuth2User.getDisplayName(),oAuth2User.getName());
            model.addAttribute("user", userDTO);
        } else {
            UserDTO userDTO = new UserDTO();
            model.addAttribute("user", userDTO);
        }
        return "register";
    }

    /**
     * Login Get endpoint.
     *
     * @return the view "login"
     */
    @GetMapping("/login")
    public String login(){
        logger.info("request the login page");
        return "login";
    }

    /**
     * Transfer Get endpoint.
     *
     * @param model the model
     * @param page  the page
     * @return the "transfer" view with pagination and the attributes :
     * "connections", "transactions" and "pageNumbers"
     */
    @GetMapping("/transfer")
    public String transfer(Model model, Optional<Integer> page){
        logger.info("request the transfer page");
        // to get the current user
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();

        // the following code handles the send Money functionality into the view
        UserDTO currentUserDTO = userService.mapToUserDto(currentUser);
        List<UserDTO> connections = userService.getConnections(currentUserDTO);
        model.addAttribute("connections", connections);// to set the dropdown of connections into the view

        // the following code handles the transactions pagination
        int currentPage = page.orElse(1);
        final Page<TransactionDTO> pageTransactions = transactionService.getTransactions(
                currentUser.getEmail(),
                PageRequest.of(currentPage - 1, 3, Sort
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

    /**
     * Contact Get endpoint.
     *
     * @param model the model
     * @param page  the page
     * @return the "contact" view with pagination and the attributes :
     * "connections" and "pageNumbers"
     */
    @GetMapping("/contact")
    public String contact(Model model, Optional<Integer> page){
        logger.info("request the contact page");
        // to get the current user
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();

        // the following code handles the connections pagination
        int currentPage = page.orElse(1);
        final Page<UserDTO> pageConnections = userService.getConnections(
                currentUser.getEmail(),
                PageRequest.of(currentPage - 1, 3, Sort.by(
                        Sort.Direction.ASC,"user1")));
        model.addAttribute("connections", pageConnections);
        int totalPages = pageConnections.getTotalPages();
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        return "contact";
    }

    /**
     * Add connection Get endpoint.
     *
     * @param model the model
     * @return the view "addConnection" with the attribute "user"
     */
    @GetMapping("/addConnection")
    public String addConnection(Model model){
        logger.info("request the add connection page");
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        return "addConnection";
    }

    /**
     * Delete connection Get endpoint.
     *
     * @param email the email to delete
     * @param model the model
     * @return the view "deleteConnection" with the attribute "user"
     */
    @GetMapping("/deleteConnection")
    public String deleteConnection(String email, Model model){
        logger.info("request the delete connection page");
        User connectionUser = userService.loadUserByUsername(email);
        UserDTO user = userService.mapToUserDto(connectionUser);
        model.addAttribute("user", user);
        return "deleteConnection";
    }

    /**
     * Profile Get endpoint.
     *
     * @param model the model
     * @return the view "profile" with the attribute "user"
     */
    @GetMapping("/profile")
    public String profile(Model model){
        logger.info("request the profile page");
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Edit name Get endpoint.
     *
     * @param model the model
     * @return the view "editName" with the attribute "user"
     */
    @GetMapping("/editName")
    public String editName(Model model){
        logger.info("request the editName page");
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        return "editName";
    }

    /**
     * Edit password Get endpoint.
     *
     * @param model the model
     * @return the view "editPassword" with the attribute "password"
     */
    @GetMapping("/editPassword")
    public String editPassword(Model model){
        logger.info("request the editPassword page");
        PasswordDTO password = new PasswordDTO();
        model.addAttribute("password", password);
        return "editPassword";
    }

    /**
     * Deposit Get endpoint.
     *
     * @param model the model
     * @return the view "deposit" with the attribute "user"
     */
    @GetMapping("/deposit")
    public String deposit(Model model){
        logger.info("request the deposit page");
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        return "deposit";
    }

    /**
     * Withdraw Get endpoint.
     *
     * @param model the model
     * @return the view "withdraw" with the attribute "user"
     */
    @GetMapping("/withdraw")
    public String withdraw(Model model){
        logger.info("request the withdraw page");
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        return "withdraw";
    }

    /**
     * Send money Get endpoint.
     *
     * @param email the email to send Money to
     * @param model the model
     * @return the view "sendMoney" with the attributes :
     * "receiverUser" and "user"
     */
    @GetMapping("/sendMoney")
    public String sendMoney(String email, Model model){
        logger.info("request the send money page");
        User connectionUser = userService.loadUserByUsername(email);
        UserDTO connectionUserDto = userService.mapToUserDto(connectionUser);
        model.addAttribute("receiverUser", connectionUserDto);
        User currentUser = authenticatedUserProvider.getAuthenticatedUser();
        UserDTO user = userService.mapToUserDto(currentUser);
        model.addAttribute("user", user);
        return "sendMoney";
    }
}
