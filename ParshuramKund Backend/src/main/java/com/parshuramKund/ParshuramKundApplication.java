package com.parshuramKund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;




import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
	public class ParshuramKundApplication {

	    public static void main(String[] args) {

	        SpringApplication.run(
	            ParshuramKundApplication.class,
	            args
	        );
	    }
}