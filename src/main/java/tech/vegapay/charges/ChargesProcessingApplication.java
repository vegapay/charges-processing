package tech.vegapay.charges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = "tech.vegapay.*")
public class ChargesProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargesProcessingApplication.class, args);
	}

}
