package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.UserService;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

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
    public String transfer(){
        logger.info("request the transfer page");
        return "transfer";
    }

    @GetMapping("/profile")
    public String profile(Model model){
        logger.info("request the profile page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.loadUserByUsername(auth.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/update")
    public String update(Model model){
        logger.info("request the update page");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User existingUser = userService.loadUserByUsername(auth.getName());
        UserDTO user = userService.mapToUserDto(existingUser);
        model.addAttribute("user", user);
        return "update";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("user") UserDTO userDto, BindingResult bindingResult, Model model) {
        logger.info("request the update of the user {}", userDto.getName());
        if (bindingResult.hasErrors()) {
            return "update";
        }
        try {
            userService.update(userDto);
            return "redirect:/update?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "update";
        }
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

    @GetMapping("/contact")
    public String contact(){
        return "contact";
    }


}
