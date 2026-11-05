package com.promou.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promou.aplicacion.eventos.SolicitudRevisada;
import com.promou.aplicacion.notificacion.NotificacionService;
import com.promou.aplicacion.notificacion.NotificacionService.Mensaje;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** HU 1.2.3 esc. 6: el representante recibe el resultado de la revisión a través del adaptador. */
class NotificacionAfiliacionListenerTest {

    private final List<Mensaje> enviados = new ArrayList<>();
    private final NotificacionService adaptadorFalso = enviados::add;
    private final NotificacionAfiliacionListener listener = new NotificacionAfiliacionListener(adaptadorFalso);

    @Test
    void notificaAprobacionAlRepresentante() {
        listener.alRevisarSolicitud(new SolicitudRevisada("ana@cafe.com", "Ana Ruiz", "Café Central",
                EstadoSolicitud.APROBADA, null));

        assertEquals(1, enviados.size());
        Mensaje m = enviados.get(0);
        assertEquals("ana@cafe.com", m.destinatario());
        assertTrue(m.asunto().contains("aprobada"));
        assertTrue(m.cuerpo().contains("APROBADA"));
    }

    @Test
    void notificaRechazoConObservaciones() {
        listener.alRevisarSolicitud(new SolicitudRevisada("ana@cafe.com", "Ana Ruiz", "Café Central",
                EstadoSolicitud.RECHAZADA, "NIT no coincide"));

        Mensaje m = enviados.get(0);
        assertTrue(m.cuerpo().contains("RECHAZADA"));
        assertTrue(m.cuerpo().contains("NIT no coincide"));
    }

    @Test
    void unFalloDelProveedorNoPropagaLaExcepcion() {
        NotificacionAfiliacionListener conFallo = new NotificacionAfiliacionListener(m -> {
            throw new IllegalStateException("SMTP caído");
        });
        conFallo.alRevisarSolicitud(new SolicitudRevisada("a@b.co", "A", "B", EstadoSolicitud.APROBADA, null));
    }
}
