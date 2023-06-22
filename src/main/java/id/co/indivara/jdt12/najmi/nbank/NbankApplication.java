package id.co.indivara.jdt12.najmi.nbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableAutoConfiguration
public class NbankApplication {
	public static void main(String[] args) {
		SpringApplication.run(NbankApplication.class, args);
	}

}
