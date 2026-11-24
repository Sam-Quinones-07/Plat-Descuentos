package com.promou.aplicacion.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adaptador para desarrollo local: sin proveedor de correo configurado, el mensaje se escribe en el log. */
public class NotificacionConsolaAdapter implements NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionConsolaAdapter.class);

    @Override
    public void enviar(Mensaje mensaje) {
        LOG.info("""
                [Correo simulado - sin proveedor configurado]
                Para: {}
                Asunto: {}
                {}""", mensaje.destinatario(), mensaje.asunto(), mensaje.cuerpo());
    }
}
