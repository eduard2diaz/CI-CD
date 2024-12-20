package com.demo.greet_example.greet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController {

    @Value("${spring.application.name:default value}")
    private String environment;

    @GetMapping("")
    @ResponseBody
    public Saludo greet(@RequestParam(defaultValue = "mundo") String name) {
        return new Saludo(name, environment);
    }
}
