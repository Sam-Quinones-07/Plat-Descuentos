package com.promou;

import com.promou.config.PromoUProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/** La autenticación es propia (token opaco + SesionFilter): no se usa el usuario en memoria de Spring. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(PromoUProperties.class)
@EnableScheduling
public class PromoUApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromoUApplication.class, args);
    }
}
