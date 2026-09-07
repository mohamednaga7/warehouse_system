package com.mohamednagah.warehouse_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController {

    @GetMapping
    public String greetings() {
        return "<h1>Hello World</h1>";
    }
}
