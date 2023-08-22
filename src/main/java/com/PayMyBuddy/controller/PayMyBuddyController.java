package com.PayMyBuddy.controller;

import com.PayMyBuddy.PayMyBuddyApplication;
import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

/**
 * Centralise the endpoints and calls the right service in attempt to find and send the response or create/delete/update data's.
 */
@RestController
public class PayMyBuddyController {

    private static final Logger logger = LogManager.getLogger(PayMyBuddyApplication.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "get all the users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The list of users")
    })
    @GetMapping("/user")
    public Iterable<User> getAllUsers() {
        logger.info("request the list of users");
        Iterable<User> result = userService.findAll();
        logger.info("response with the list of users");
        return result;
    }
}
