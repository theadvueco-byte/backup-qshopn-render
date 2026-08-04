package com.synix.uwait_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.sql.DataSource;
import java.sql.Connection;

@EnableCaching
@EnableScheduling
@ComponentScan
@SpringBootApplication
public class UwaitBackendApplication {


	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(e ->
				System.setProperty(e.getKey(), e.getValue())
		);

		SpringApplication.run(UwaitBackendApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onStartup() {
		System.out.println();
		try (Connection conn = dataSource.getConnection()) {
			if (conn.isValid(2)) {
				System.out.println("Database Connected");
			} else {
				System.out.println("Database NOT Connected");
			}
		} catch (Exception e) {
			System.out.println("Database Connection Failed: " + e.getMessage());
		}
		System.out.println("Server running on port 8080");
		System.out.println("http://localhost:8080/swagger-ui/index.html#/");
		System.out.println();

		System.out.println("==============================================================================");
		System.out.println("   ____          _            __ _ _       _       __     __   ____  ");
		System.out.println("  /\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\  | |  _(_)_ \\ \\   / / |  _ \\ ");
		System.out.println(" ( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\  | |   | |   \\ \\ / /  | |_| |");
		System.out.println("  \\/  ___)| |_)| | | | | || (_| |  ) ) )  | |   | |    \\ V /   | | ");
		System.out.println("   '  |____| .__|_| |_|_| |_\\__, | / / /  |_|___|___|   \\_/    |____/ ");
		System.out.println("  =========|_|==============|___/=/_/_/_/                               ");
		System.out.println("==============================================================================");
		System.out.println(); 
	}
}
