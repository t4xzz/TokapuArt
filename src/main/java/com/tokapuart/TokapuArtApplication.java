package com.tokapuart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TokapuArtApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokapuArtApplication.class, args);
        System.out.println("\n" +
                "╔════════════════════════════════════════╗\n" +
                "║   TOKAPU ART API - RUNNING            ║\n" +
                "║   Port: 8080                          ║\n" +
                "║   Status: ✓ Active                    ║\n" +
                "╚════════════════════════════════════════╝\n");
    }
}
