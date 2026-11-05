package com.promou.aplicacion.notificacion;

/**
 * Patrón Adapter (HU 1.2.3 esc. 6) - interfaz objetivo ("INotificacionService" en el documento de diseño).
 * Los casos de uso dependen solo de esta interfaz propia; cada adaptador la traduce a un proveedor concreto
 * (SMTP con {@link CorreoSmtpAdapter}, o la consola en desarrollo con {@link NotificacionConsolaAdapter}).
 * Si cambia el proveedor, se escribe otro adaptador sin tocar los servicios (Protected Variations, Low Coupling).
 * El mismo adaptador servirá en el Release 2 para la confirmación de registro y el restablecimiento de contraseña.
 */
public interface NotificacionService {

    void enviar(Mensaje mensaje);

    /** Mensaje independiente del proveedor. */
    record Mensaje(String destinatario, String asunto, String cuerpo) {
    }
}
