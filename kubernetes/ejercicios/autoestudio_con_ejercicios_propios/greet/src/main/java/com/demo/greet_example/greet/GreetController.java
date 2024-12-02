package com.demo.greet_example.greet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController {

    private static final Logger log = LoggerFactory.getLogger(GreetController.class);
    @Value("${spring.application.name:default value}")
    private String environment;

    @GetMapping("")
    @ResponseBody
    public Saludo greet(@RequestParam(defaultValue = "mundo") String name) {
        log.info("Un saludo esta siendo enviado a " + name);
        return new Saludo(name, environment);
    }
}
