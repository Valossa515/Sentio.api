package br.com.aftersunrise.sentio.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sentio API - Text Analysis")
                        .version("v1.0")
                        .description("API para análise de sentimento e entidades em textos")
                        .contact(new Contact().name("Felipe Martins").email("fe.mmo515@gmail.com"))
                );
    }
}
