package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.service.UserService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Centralise the endpoints and calls the right service in attempt to find and send the response or create/delete/update data's.
 */
@Controller
public class PayMyBuddyController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @GetMapping("/index")
    public String home(){
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        logger.info("request the form page");
        UserDTO userDTO = new UserDTO();
        model.addAttribute("user", userDTO);
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@ModelAttribute("user") UserDTO userDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirAttrs) {

        if(result.hasErrors()){
            model.addAttribute("user", userDto);
            return "/register";
        }

        try {
            userService.saveUser(userDto);
            return "redirect:/register?success";
        } catch (Exception e) {
            redirAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/users")
    public String users(Model model){
        List<UserDTO> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

}
