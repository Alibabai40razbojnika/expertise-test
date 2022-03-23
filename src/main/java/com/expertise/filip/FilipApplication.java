package com.expertise.filip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
    "com.expertise.filip.controller",
    "com.expertise.filip.function",
    "com.expertise.filip.scheduler",
    "com.expertise.filip.util",
    "com.expertise.filip.wrapper"})
public class FilipApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilipApplication.class, args);
    }

}
