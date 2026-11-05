package com.promou.web;

import com.promou.aplicacion.GestionDescuentosService;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoRequest;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.seguridad.UsuarioAutenticado;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Lado establecimiento: HU 2.1.1 Publicar descuento. */
@RestController
@RequestMapping("/api/establecimientos/me")
public class DescuentoController {

    private final GestionDescuentosService gestion;

    public DescuentoController(GestionDescuentosService gestion) {
        this.gestion = gestion;
    }

    @GetMapping("/descuentos")
    public List<DescuentoResponse> misDescuentos(@AuthenticationPrincipal UsuarioAutenticado u) {
        return gestion.listarPropios(u.usuarioId());
    }

    @PostMapping("/descuentos")
    @ResponseStatus(HttpStatus.CREATED)
    public DescuentoResponse publicar(@AuthenticationPrincipal UsuarioAutenticado u,
                                      @RequestBody DescuentoRequest r) {
        return gestion.publicar(u.usuarioId(), r);
    }
}
