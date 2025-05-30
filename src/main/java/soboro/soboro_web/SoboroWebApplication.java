package soboro.soboro_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class SoboroWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(SoboroWebApplication.class, args);
	}
}