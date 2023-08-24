package com.PayMyBuddy.controller;

import ch.qos.logback.core.util.CachingDateFormatter;
import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.dto.UserDTO;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.UserAlreadyExistException;
import com.PayMyBuddy.service.UserService;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Date;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Centralise the endpoints and calls the right service in attempt to find and send the response or create/delete/update data's.
 */
@Controller
public class PayMyBuddyController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    private ModelAndView mav = new ModelAndView();


    @GetMapping("/user/registration")
    public String showRegistrationForm(WebRequest request, Model model) {
        logger.info("request the form page");
        UserDTO userDto = new UserDTO();
        model.addAttribute("user", userDto);
        return "registration";
    }


    @PostMapping("/user/registration")
    public ModelAndView registerUserAccount(
            @ModelAttribute("user") @Valid UserDTO userDto,
            HttpServletRequest request,
            Errors errors) {
        logger.info("request for creating a user");
        try {
            User registered = userService.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException uaeEx) {
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }
        return new ModelAndView("successRegister", "user", userDto);
    }

    @Target({TYPE, FIELD, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Constraint(validatedBy = EmailValidator.class)
    @Documented
    public @interface ValidEmail {
        String message() default "Invalid email";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
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
