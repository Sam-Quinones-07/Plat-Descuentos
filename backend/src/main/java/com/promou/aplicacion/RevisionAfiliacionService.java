package com.promou.aplicacion;

import com.promou.aplicacion.dto.AfiliacionDtos.SolicitudDetalle;
import com.promou.aplicacion.dto.AfiliacionDtos.SolicitudResumen;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import com.promou.dominio.usuario.Administrador;
import com.promou.dominio.usuario.AdministradorRepository;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.EstablecimientoRepository;
import com.promou.aplicacion.eventos.SolicitudRevisada;
import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 1.2.3: el administrador revisa las solicitudes de afiliación. */
@Service
public class RevisionAfiliacionService {

    private final EstablecimientoRepository establecimientos;
    private final AdministradorRepository administradores;
    private final ApplicationEventPublisher eventos;
    private final Clock reloj;

    public RevisionAfiliacionService(EstablecimientoRepository establecimientos,
                                     AdministradorRepository administradores,
                                     ApplicationEventPublisher eventos,
                                     Clock reloj) {
        this.establecimientos = establecimientos;
        this.administradores = administradores;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    /** @param vista pending | reviewed | (vacío = todas), igual que las pestañas del front. */
    @Transactional(readOnly = true)
    public List<SolicitudResumen> listar(String vista) {
        return establecimientos.listarPorEstados(estadosDe(vista)).stream()
                .map(e -> new SolicitudResumen(e.getSolicitud().getId(), e.getNombreComercial(), e.getNit(),
                        e.getRepresentanteNombre(), e.getSolicitud().getFechaSolicitud(),
                        e.getEstadoAfiliacion().etiquetaFront()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudDetalle detalle(UUID solicitudId) {
        return aDetalle(cargar(solicitudId));
    }

    /** Aprobar o rechazar. Si ya fue revisada, el estado (State) lanza un conflicto con el estado actual. */
    @Transactional
    public SolicitudDetalle decidir(UUID adminId, UUID solicitudId, boolean aprobar, String observaciones) {
        Administrador admin = administradores.findById(adminId).orElseThrow(
                () -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
        Establecimiento e = cargar(solicitudId);
        String obs = Texto.nuloSiVacio(observaciones);
        admin.revisarSolicitud(e, aprobar, obs, reloj.instant());
        // Esc. 6: el listener notifica al representante cuando la transacción se confirma (Observer + Adapter)
        eventos.publishEvent(new SolicitudRevisada(e.getRepresentanteCorreo(), e.getRepresentanteNombre(),
                e.getNombreComercial(), e.getEstadoAfiliacion(), obs));
        return aDetalle(e);
    }

    private Establecimiento cargar(UUID solicitudId) {
        return establecimientos.buscarPorSolicitud(solicitudId).orElseThrow(
                () -> ReglaNegocioException.noEncontrado("SOLICITUD_NO_ENCONTRADA", "La solicitud no existe."));
    }

    private static Set<EstadoSolicitud> estadosDe(String vista) {
        if (Texto.vacio(vista)) {
            return EnumSet.allOf(EstadoSolicitud.class);
        }
        return switch (vista.trim().toLowerCase()) {
            case "pending" -> EnumSet.of(EstadoSolicitud.PENDIENTE);
            case "reviewed" -> EnumSet.of(EstadoSolicitud.APROBADA, EstadoSolicitud.RECHAZADA);
            default -> throw ReglaNegocioException.validacion("DATOS_INVALIDOS", "El filtro debe ser pending o reviewed.");
        };
    }

    private static SolicitudDetalle aDetalle(Establecimiento e) {
        var s = e.getSolicitud();
        return new SolicitudDetalle(s.getId(), e.getNombreComercial(), e.getRazonSocial(), e.getNit(), e.getCorreo(),
                e.getTelefono(), e.getDireccion(), e.getRepresentanteNombre(), e.getRepresentanteDocumento(),
                e.getRepresentanteCorreo(), e.isCorreoConfirmado(), s.getEstado().etiquetaFront(),
                s.getFechaSolicitud(), s.getFechaResolucion(), s.getObservaciones());
    }
}
