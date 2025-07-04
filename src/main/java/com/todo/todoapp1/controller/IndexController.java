package com.todo.todoapp1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "index"; // This will look for index.html in templates folder
    }
}