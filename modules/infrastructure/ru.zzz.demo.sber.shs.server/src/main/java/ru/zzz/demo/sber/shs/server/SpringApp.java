package ru.zzz.demo.sber.shs.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.springframework.boot.WebApplicationType.NONE;

@SpringBootApplication
public class SpringApp {
    public static void main(String[] args) {
        // Disable automatic web environment because app starts custom reactive web server.
        new SpringApplicationBuilder(SpringApp.class).web(NONE).run(args);
    }
}
