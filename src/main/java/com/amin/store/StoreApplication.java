package com.amin.store;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StoreApplication {

    public static void main(String[] args) {

        // This will find, load, and set all .env variables as System Properties
        Dotenv.configure().systemProperties().load();

        SpringApplication.run(StoreApplication.class, args);
    }
}
