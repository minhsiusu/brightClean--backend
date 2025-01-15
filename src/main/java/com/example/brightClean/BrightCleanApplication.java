package com.example.brightClean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.example.brightClean.util.JWTConstant;

@SpringBootApplication
@EnableScheduling
public class BrightCleanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrightCleanApplication.class, args);
	}

}
