package com.promou.aplicacion;

import com.promou.aplicacion.eventos.SolicitudRevisada;
import com.promou.aplicacion.notificacion.NotificacionService;
import com.promou.aplicacion.notificacion.NotificacionService.Mensaje;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * HU 1.2.3 esc. 6: notifica al representante el resultado de la revisión.
 * Observer: escucha {@link SolicitudRevisada} solo después del commit, así un fallo del proveedor de correo
 * no deshace la decisión del administrador. Depende de la interfaz {@link NotificacionService} (Adapter),
 * no del SMTP.
 */
@Component
public class NotificacionAfiliacionListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionAfiliacionListener.class);

    private final NotificacionService notificaciones;

    public NotificacionAfiliacionListener(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alRevisarSolicitud(SolicitudRevisada evento) {
        try {
            notificaciones.enviar(construir(evento));
        } catch (RuntimeException e) {
            // El representante igual verá el estado al iniciar sesión (HU 1.2.4).
            LOG.warn("No se pudo notificar el resultado de la afiliación de {}: {}", evento.nombreComercial(),
                    e.getMessage());
        }
    }

    static Mensaje construir(SolicitudRevisada e) {
        boolean aprobada = e.estado() == EstadoSolicitud.APROBADA;
        String asunto = aprobada
                ? "PromoU: tu solicitud de afiliación fue aprobada"
                : "PromoU: resultado de tu solicitud de afiliación";
        StringBuilder cuerpo = new StringBuilder()
                .append("Hola, ").append(e.nombreRepresentante()).append(".\n\n")
                .append("La solicitud de afiliación de \"").append(e.nombreComercial()).append("\" fue ")
                .append(aprobada ? "APROBADA" : "RECHAZADA").append(".\n");
        if (e.observaciones() != null) {
            cuerpo.append("\nObservaciones del administrador: ").append(e.observaciones()).append('\n');
        }
        cuerpo.append(aprobada
                ? "\nYa puedes iniciar sesión con el correo del establecimiento y publicar tus descuentos.\n"
                : "\nSi tienes dudas, comunícate con el equipo de PromoU.\n");
        cuerpo.append("\n— Equipo PromoU");
        return new Mensaje(e.correoRepresentante(), asunto, cuerpo.toString());
    }
}
