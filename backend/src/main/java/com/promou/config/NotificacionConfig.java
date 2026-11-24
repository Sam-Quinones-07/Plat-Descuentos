package com.promou.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promou.aplicacion.notificacion.CorreoBrevoAdapter;
import com.promou.aplicacion.notificacion.CorreoSmtpAdapter;
import com.promou.aplicacion.notificacion.NotificacionConsolaAdapter;
import com.promou.aplicacion.notificacion.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/** Elige el adaptador de correo: Brevo (BREVO_API_KEY), SMTP (SMTP_HOST) o consola, en ese orden. */
@Configuration
public class NotificacionConfig {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionConfig.class);

    @Bean
    NotificacionService notificacionService(@Value("${spring.mail.host:}") String host,
                                            ObjectProvider<JavaMailSender> correo, PromoUProperties props,
                                            ObjectMapper json) {
        PromoUProperties.Notificaciones n = props.notificaciones();
        if (!n.brevoApiKey().isBlank()) {
            LOG.info("Correo: API de Brevo");
            return new CorreoBrevoAdapter(json, n.brevoApiKey().trim(), n.remitente());
        }
        JavaMailSender proveedor = host.isBlank() ? null : correo.getIfAvailable();
        if (proveedor != null) {
            LOG.info("Correo: SMTP {}", host);
            return new CorreoSmtpAdapter(proveedor, n.remitente());
        }
        LOG.info("Correo: sin proveedor configurado, los mensajes se escriben en el log");
        return new NotificacionConsolaAdapter();
    }
}
