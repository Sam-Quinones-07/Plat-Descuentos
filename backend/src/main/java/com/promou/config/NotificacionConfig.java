package com.promou.config;

import com.promou.aplicacion.notificacion.CorreoSmtpAdapter;
import com.promou.aplicacion.notificacion.NotificacionConsolaAdapter;
import com.promou.aplicacion.notificacion.NotificacionService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/** Elige el adaptador: SMTP si hay un servidor de correo configurado (SMTP_HOST), consola en otro caso. */
@Configuration
public class NotificacionConfig {

    @Bean
    NotificacionService notificacionService(@Value("${spring.mail.host:}") String host,
                                            ObjectProvider<JavaMailSender> correo, PromoUProperties props) {
        JavaMailSender proveedor = host.isBlank() ? null : correo.getIfAvailable();
        return proveedor != null
                ? new CorreoSmtpAdapter(proveedor, props.notificaciones().remitente())
                : new NotificacionConsolaAdapter();
    }
}
