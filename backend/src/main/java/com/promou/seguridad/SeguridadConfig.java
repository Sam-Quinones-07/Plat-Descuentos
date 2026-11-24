package com.promou.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promou.comun.LimitadorPeticiones;
import com.promou.config.PromoUProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Sesión en cookie HttpOnly (ver {@link CookieSesion}). El front llama a la API por su mismo dominio, así que no se
 * necesita CORS. La protección CSRF propia de Spring se reemplaza por la cabecera obligatoria de
 * {@link ProteccionPeticionesFilter} más SameSite=Strict.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadena(HttpSecurity http, SesionService sesiones, ObjectMapper json, PromoUProperties props,
                               Clock reloj) throws Exception {
        var limitePorIp = new LimitadorPeticiones(props.seguridad().peticionesAuthPorMinutoIp(), Duration.ofMinutes(1));
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/logout", "/api/auth/contrasena").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/estudiantes/**", "/api/descuentos/**")
                        .hasAuthority("ROLE_ESTUDIANTE")
                        .requestMatchers("/api/establecimientos/**").hasAuthority("ROLE_ESTABLECIMIENTO")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMINISTRADOR")
                        .anyRequest().denyAll())
                .addFilterBefore(new ProteccionPeticionesFilter(limitePorIp, json, reloj),
                        UsernamePasswordAuthenticationFilter.class)
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

    private static void escribir(ObjectMapper json, HttpServletResponse res, int status, String codigo, String mensaje)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), Map.of("codigo", codigo, "mensaje", mensaje));
    }
}
