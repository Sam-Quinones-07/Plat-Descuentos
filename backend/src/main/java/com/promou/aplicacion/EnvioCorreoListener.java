package com.promou.aplicacion;

import com.promou.aplicacion.eventos.CorreoPendiente;
import com.promou.aplicacion.notificacion.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observer: envía los enlaces por correo después del commit. Si el proveedor falla, el registro no se deshace:
 * el usuario puede pedir un enlace nuevo (HU 1.2.2 esc. 5).
 */
@Component
public class EnvioCorreoListener {

    private static final Logger LOG = LoggerFactory.getLogger(EnvioCorreoListener.class);

    private final NotificacionService notificaciones;

    public EnvioCorreoListener(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviar(CorreoPendiente evento) {
        try {
            notificaciones.enviar(evento.mensaje());
        } catch (RuntimeException e) {
            LOG.warn("No se pudo enviar el correo \"{}\": {}", evento.mensaje().asunto(), e.getMessage());
        }
    }
}
