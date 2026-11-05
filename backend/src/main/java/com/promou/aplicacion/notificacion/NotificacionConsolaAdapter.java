package com.promou.aplicacion.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adaptador para desarrollo local: sin SMTP configurado, el mensaje se escribe en el log del backend. */
public class NotificacionConsolaAdapter implements NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionConsolaAdapter.class);

    @Override
    public void enviar(Mensaje mensaje) {
        LOG.info("""
                [Notificación simulada - sin SMTP configurado]
                Para: {}
                Asunto: {}
                {}""", mensaje.destinatario(), mensaje.asunto(), mensaje.cuerpo());
    }
}
