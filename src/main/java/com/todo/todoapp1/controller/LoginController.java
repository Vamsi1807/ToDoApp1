package com.todo.todoapp1.controller;

import com.todo.todoapp1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // Remove this method entirely if IndexController handles "/"
    // Or change its mapping if it's meant for something else.
    // For example, if you want "home" to be specifically "/myhome"
    // @GetMapping("/myhome")
    // public String home() {
    //     return "index";
    // }


    @GetMapping("/login")
    public String showLoginForm() {
        System.out.println("login form");
        return "login";
    }


    // Remove the custom login POST mapping since Spring Security will handle it
}