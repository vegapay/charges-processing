package tech.vegapay.charges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "tech.vegapay.*")
public class ChargesProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargesProcessingApplication.class, args);
	}

}
