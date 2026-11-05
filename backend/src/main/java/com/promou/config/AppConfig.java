package com.promou.config;

import com.promou.comun.DominioInstitucional;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    /** "Hoy" se calcula en hora de Colombia: la vigencia de un descuento termina a medianoche de Bogotá. */
    @Bean
    Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    DominioInstitucional dominioInstitucional(PromoUProperties props) {
        return new DominioInstitucional(props.seguridad().dominiosInstitucionales());
    }
}
