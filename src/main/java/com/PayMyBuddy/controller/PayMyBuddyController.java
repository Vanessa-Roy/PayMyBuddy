package com.PayMyBuddy.controller;

import com.PayMyBuddy.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayMyBuddyController {

    @Autowired
    private UserService userService;
}
