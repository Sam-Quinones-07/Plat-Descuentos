package com.promou.aplicacion.notificacion;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Adaptador: traduce {@link NotificacionService} a la API del proveedor SMTP (adaptado: {@link JavaMailSender}).
 * Se usa cuando hay un servidor de correo configurado (spring.mail.host / SMTP_HOST).
 */
public class CorreoSmtpAdapter implements NotificacionService {

    private final JavaMailSender proveedor;
    private final String remitente;

    public CorreoSmtpAdapter(JavaMailSender proveedor, String remitente) {
        this.proveedor = proveedor;
        this.remitente = remitente;
    }

    @Override
    public void enviar(Mensaje mensaje) {
        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setFrom(remitente);
        correo.setTo(mensaje.destinatario());
        correo.setSubject(mensaje.asunto());
        correo.setText(mensaje.cuerpo());
        proveedor.send(correo);
    }
}
