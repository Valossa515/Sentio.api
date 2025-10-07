package br.com.aftersunrise.sentio.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "br.com.aftersunrise.sentio")
@EnableJpaRepositories(basePackages = "br.com.aftersunrise.sentio.infrastructure.repositories")
@EntityScan(basePackages = "br.com.aftersunrise.sentio.domain.models")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
