package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import com.promou.dominio.usuario.Administrador;
import com.promou.dominio.usuario.Establecimiento;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AfiliacionTest {

    private static final Instant AHORA = Instant.parse("2026-10-26T12:00:00Z");

    private Establecimiento nuevo() {
        return Establecimiento.solicitarAfiliacion("contacto@x.com", "hash",
                new Establecimiento.Datos("X", "X SAS", "901-234", "3001234567", "Calle 1", "Ana", "12345", "ana@x.com"), AHORA);
    }

    @Test
    void laSolicitudNaceEnPendienteYNoPermiteIniciarSesion() {
        Establecimiento e = nuevo();
        assertEquals(EstadoSolicitud.PENDIENTE, e.getEstadoAfiliacion());
        e.confirmarCorreo();
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, e::verificarPuedeIniciarSesion);
        assertEquals("AFILIACION_PENDIENTE", ex.getCodigo());
    }

    @Test
    void sinCorreoConfirmadoNoHaySesionAunqueEsteAprobado() {
        Establecimiento e = nuevo();
        Administrador admin = new Administrador("admin@x.com", "h", "Admin", 1, AHORA);
        admin.revisarSolicitud(e, true, null, AHORA);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, e::verificarPuedeIniciarSesion);
        assertEquals("CORREO_NO_CONFIRMADO", ex.getCodigo());
        e.confirmarCorreo();
        assertDoesNotThrow(e::verificarPuedeIniciarSesion);
        assertDoesNotThrow(e::exigirOperativo);
    }

    @Test
    void rechazadaNoPuedeIniciarSesionNiOperar() {
        Establecimiento e = nuevo();
        e.confirmarCorreo();
        new Administrador("admin@x.com", "h", "Admin", 1, AHORA).revisarSolicitud(e, false, "NIT no coincide", AHORA);
        assertEquals("AFILIACION_RECHAZADA",
                assertThrows(ReglaNegocioException.class, e::verificarPuedeIniciarSesion).getCodigo());
        assertEquals("ESTABLECIMIENTO_NO_AFILIADO",
                assertThrows(ReglaNegocioException.class, e::exigirOperativo).getCodigo());
    }

    @Test
    void unaSolicitudYaRevisadaNoSeRevisaDeNuevo() {
        Establecimiento e = nuevo();
        Administrador admin = new Administrador("admin@x.com", "h", "Admin", 1, AHORA);
        admin.revisarSolicitud(e, true, null, AHORA);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> admin.revisarSolicitud(e, false, null, AHORA));
        assertEquals("SOLICITUD_YA_REVISADA", ex.getCodigo());
        assertEquals(EstadoSolicitud.APROBADA, e.getEstadoAfiliacion());
        assertFalse(EstadoSolicitud.PENDIENTE.revisada());
    }

    @Test
    void elNitSeNormalizaParaDetectarDuplicados() {
        assertEquals("9012345678", Establecimiento.normalizarNit("901234567-8"));
    }
}
