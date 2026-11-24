package com.promou.web;

import com.promou.aplicacion.GestionDescuentosService;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoRequest;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.seguridad.UsuarioAutenticado;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Lado establecimiento: HU 2.1.1 (publicar), 2.1.2 (editar) y 2.1.3 (desactivar). */
@RestController
@RequestMapping("/api/establecimientos/me/descuentos")
public class DescuentoController {

    private final GestionDescuentosService gestion;

    public DescuentoController(GestionDescuentosService gestion) {
        this.gestion = gestion;
    }

    @GetMapping
    public List<DescuentoResponse> misDescuentos(@AuthenticationPrincipal UsuarioAutenticado u) {
        return gestion.listarPropios(u.usuarioId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DescuentoResponse publicar(@AuthenticationPrincipal UsuarioAutenticado u, @RequestBody DescuentoRequest r) {
        return gestion.publicar(u.usuarioId(), r);
    }

    @GetMapping("/{id}")
    public DescuentoResponse detalle(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return gestion.obtenerPropio(u.usuarioId(), id);
    }

    @PutMapping("/{id}")
    public DescuentoResponse editar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id,
                                    @RequestBody DescuentoRequest r) {
        return gestion.editar(u.usuarioId(), id, r);
    }

    @PostMapping("/{id}/desactivar")
    public DescuentoResponse desactivar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return gestion.desactivar(u.usuarioId(), id);
    }
}
