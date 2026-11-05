package com.promou.web;

import com.promou.aplicacion.RevisionAfiliacionService;
import com.promou.aplicacion.dto.AfiliacionDtos.DecisionRequest;
import com.promou.aplicacion.dto.AfiliacionDtos.SolicitudDetalle;
import com.promou.aplicacion.dto.AfiliacionDtos.SolicitudResumen;
import com.promou.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU 1.2.3: revisión de solicitudes de afiliación. */
@RestController
@RequestMapping("/api/admin/afiliaciones")
public class AfiliacionController {

    private final RevisionAfiliacionService revision;

    public AfiliacionController(RevisionAfiliacionService revision) {
        this.revision = revision;
    }

    /** ?estado=pending (pestaña Pendientes) | reviewed (pestaña Revisadas) | sin parámetro = todas. */
    @GetMapping
    public List<SolicitudResumen> listar(@RequestParam(required = false) String estado) {
        return revision.listar(estado);
    }

    @GetMapping("/{id}")
    public SolicitudDetalle detalle(@PathVariable UUID id) {
        return revision.detalle(id);
    }

    @PostMapping("/{id}/aprobar")
    public SolicitudDetalle aprobar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id,
                                    @Valid @RequestBody(required = false) DecisionRequest r) {
        return revision.decidir(u.usuarioId(), id, true, r == null ? null : r.observations());
    }

    @PostMapping("/{id}/rechazar")
    public SolicitudDetalle rechazar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id,
                                     @Valid @RequestBody(required = false) DecisionRequest r) {
        return revision.decidir(u.usuarioId(), id, false, r == null ? null : r.observations());
    }
}
