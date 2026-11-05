package com.promou.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "promou")
public record PromoUProperties(
        @DefaultValue Cors cors,
        @DefaultValue Admin admin,
        @DefaultValue Seguridad seguridad,
        @DefaultValue Notificaciones notificaciones) {

    public record Notificaciones(@DefaultValue("PromoU <no-reply@promou.app>") String remitente) {
    }

    public record Cors(@DefaultValue("http://localhost:8443,http://localhost:5173") List<String> origenes) {
    }

    public record Admin(@DefaultValue("admin@plataforma.com") String correo,
                        @DefaultValue("") String contrasena,
                        @DefaultValue("Administrador") String nombre) {
    }

    public record Seguridad(
            @DefaultValue("udea.edu.co") List<String> dominiosInstitucionales,
            @DefaultValue("30m") Duration inactividadSesion,
            @DefaultValue("5") int maxIntentosFallidos,
            @DefaultValue("15m") Duration ventanaIntentos,
            @DefaultValue("15m") Duration duracionBloqueo,
            @DefaultValue("365d") Duration vigenciaCredencial) {
    }
}
