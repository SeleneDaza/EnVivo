package com.edu.uptc.EnVivo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.time.Instant;

@SpringBootApplication
public class EnVivoApplication {

	private static final Logger logger = LoggerFactory.getLogger(EnVivoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(EnVivoApplication.class, args);
		logger.info("event=application_start status=started timestamp={} ", Instant.now().toString());
	}

}
