package com.demo.greet_example.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MyStartupApplicationRunner implements ApplicationRunner {

    @Value("${file.save_path}")
    private String savePath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!Files.exists(Path.of(savePath))) {
            Files.createDirectories(Path.of(savePath));
        }
    }
}
