package com.personeltakip.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personel Takip Sistemi API")
                        .version("1.0.0")
                        .description("Personel Takip Sistemi için RESTful API dokümantasyonu. " +
                                "Bu API, personel yönetimi, katılım takibi ve QR kod işlemleri için kullanılır.")
                        .contact(new Contact()
                                .name("Personel Takip Sistemi")
                                .email("info@personeltakip.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/personeltakip")
                                .description("Geliştirme Sunucusu"),
                        new Server()
                                .url("https://api.personeltakip.com/personeltakip")
                                .description("Üretim Sunucusu")
                ));
    }
}

