package projektzespolowy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Dla wszystkich ścieżek
                .allowedOrigins("http://localhost:4200") // Zezwól na żądania z tego origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Zezwólone metody HTTP
                .allowedHeaders("*") // Zezwól na wszystkie nagłówki
                .allowCredentials(true); // Zezwól na przesyłanie ciasteczek
    }
}