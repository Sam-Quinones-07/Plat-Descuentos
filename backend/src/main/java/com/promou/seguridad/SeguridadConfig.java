package com.promou.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promou.config.PromoUProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** API sin estado: token Bearer, sin cookies, por eso CSRF no aplica. */
@Configuration
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadena(HttpSecurity http, SesionService sesiones, ObjectMapper json) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/estudiantes/**", "/api/descuentos/**")
                        .hasAuthority("ROLE_ESTUDIANTE")
                        .requestMatchers("/api/establecimientos/**").hasAuthority("ROLE_ESTABLECIMIENTO")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMINISTRADOR")
                        .anyRequest().denyAll())
                .addFilterBefore(new SesionFilter(sesiones), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            boolean expirada = Boolean.TRUE.equals(req.getAttribute(SesionFilter.ATRIBUTO_SESION_EXPIRADA));
                            escribir(json, res, 401, expirada ? "SESION_EXPIRADA" : "NO_AUTENTICADO",
                                    expirada ? "Tu sesión expiró por inactividad. Inicia sesión nuevamente."
                                            : "Debes iniciar sesión.");
                        })
                        .accessDeniedHandler((req, res, ex) ->
                                escribir(json, res, 403, "ACCESO_DENEGADO", "No tienes permiso para esta acción.")));
        return http.build();
    }

    @Bean
    CorsConfigurationSource cors(PromoUProperties props) {
        CorsConfiguration c = new CorsConfiguration();
        // Patrones: permite, por ejemplo, https://promou*.vercel.app (producción y vistas previas de Vercel)
        c.setAllowedOriginPatterns(props.cors().origenes().stream().map(String::trim).toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", c);
        return fuente;
    }

    private static void escribir(ObjectMapper json, HttpServletResponse res, int status, String codigo, String mensaje)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), Map.of("codigo", codigo, "mensaje", mensaje));
    }
}
