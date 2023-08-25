package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.UserAlreadyExistException;
import com.PayMyBuddy.service.UserService;

import javax.validation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Centralise the endpoints and calls the right service in attempt to find and send the response or create/delete/update data's.
 */
@Validated
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
    public String registration(@Valid @ModelAttribute("user") UserDTO userDto,
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
        } catch (UserAlreadyExistException e) {
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


    @Target({TYPE,ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Constraint(validatedBy = PasswordMatchesValidator.class)
    @Documented
    public @interface PasswordMatches {
        String message() default "Passwords don't match";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}
