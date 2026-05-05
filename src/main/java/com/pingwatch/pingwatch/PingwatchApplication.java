package com.pingwatch.pingwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // enables the @Scheduled auto-monitor
public class PingwatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PingwatchApplication.class, args);
	}

}
