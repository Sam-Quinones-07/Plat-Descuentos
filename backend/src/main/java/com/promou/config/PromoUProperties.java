package com.promou.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "promou")
public record PromoUProperties(
        @DefaultValue Admin admin,
        @DefaultValue Seguridad seguridad,
        @DefaultValue Notificaciones notificaciones,
        @DefaultValue Front front) {

    /**
     * remitente: "Nombre <correo>". brevoApiKey: si está definida, los correos salen por la API HTTPS de Brevo
     * (deuda técnica 2); si no, por SMTP (si hay SMTP_HOST) o por el log.
     */
    public record Notificaciones(@DefaultValue("PromoU <no-reply@promou.app>") String remitente,
                                 @DefaultValue("") String brevoApiKey) {
    }

    /** url: dirección pública del front, para armar los enlaces de los correos. */
    public record Front(@DefaultValue("http://localhost:8443") String url,
                        @DefaultValue("2026-11") String versionTerminos) {
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
            @DefaultValue("365d") Duration vigenciaCredencial,
            @DefaultValue("24h") Duration vigenciaEnlaceConfirmacion,
            @DefaultValue("1h") Duration vigenciaEnlaceRestablecimiento,
            @DefaultValue("true") boolean cookieSegura,
            @DefaultValue("30") int peticionesAuthPorMinutoIp,
            @DefaultValue("3") int correosPorVentana,
            @DefaultValue("15m") Duration ventanaCorreos) {
    }
}
