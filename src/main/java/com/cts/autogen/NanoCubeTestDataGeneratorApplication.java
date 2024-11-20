package com.cts.autogen;

import com.cts.autogen.logger.BasicConfApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class NanoCubeTestDataGeneratorApplication {

	private final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(NanoCubeTestDataGeneratorApplication.class, args);
		while (true) {
			Thread.sleep(1000);
		}
	}
}
