package com.videoprocessing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Video Processing API")
                        .version("1.0.0")
                        .description("""
                                Professional Video Processing API built with Spring Boot
                                
                                ## Features:
                                - **Level 1**: Video Upload & Metadata extraction
                                - **Level 2**: Video Trimming with precise timestamps  
                                - **Level 3**: Text/Image/Video Overlays + Watermarking
                                - **Level 4**: Async Job Queue with real-time status
                                - **Level 5**: Multiple Quality Generation (480p/720p/1080p)
                                
                                ## Indian Language Support:
                                Supports overlays in Hindi, Tamil, Telugu, Bengali, Marathi, 
                                Gujarati, Kannada, Malayalam, Punjabi, and Odia.
                                """)
                        .contact(new Contact()
                                .name("Video Processing Team")
                                .email("support@videoprocessing.com")
                                .url("https://videoprocessing.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.videoprocessing.com")
                                .description("Production Server")
                ));
    }
}
