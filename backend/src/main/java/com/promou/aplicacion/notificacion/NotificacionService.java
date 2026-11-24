package com.promou.aplicacion.notificacion;

/**
 * Patrón Adapter - interfaz objetivo ("INotificacionService" en el documento de diseño).
 * Los casos de uso dependen solo de esta interfaz propia; cada adaptador la traduce a un proveedor concreto:
 * la API de Brevo ({@link CorreoBrevoAdapter}), SMTP ({@link CorreoSmtpAdapter}) o el log ({@link NotificacionConsolaAdapter}).
 * Si cambia el proveedor, se escribe otro adaptador sin tocar los servicios (Protected Variations, Low Coupling).
 * La usan el aviso de afiliación (HU 1.2.3), la confirmación de registro (HU 1.1.2, 1.2.2) y el restablecimiento
 * de contraseña (HU 1.3.2).
 */
public interface NotificacionService {

    void enviar(Mensaje mensaje);

    /** Mensaje independiente del proveedor. */
    record Mensaje(String destinatario, String asunto, String cuerpo) {
    }
}
