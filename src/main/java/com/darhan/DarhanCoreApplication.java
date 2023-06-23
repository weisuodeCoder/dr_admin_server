package com.darhan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;


@ServletComponentScan
@SpringBootApplication
public class DarhanCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(DarhanCoreApplication.class, args);
    }
}
