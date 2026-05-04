package com.ids.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class},
        scanBasePackages = {"com.ids.api", "com.ids.logingestion", "com.ids.logingestion.fim", "com.ids.config"}
)
public class ApiApplication {
	public static void main(String[] args) {

		SpringApplication.run(ApiApplication.class, args);
		System.out.println("Executed Successfully");
	}
}
