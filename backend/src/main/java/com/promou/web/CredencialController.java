package com.promou.web;

import com.promou.aplicacion.CredencialService;
import com.promou.aplicacion.dto.DescuentoDtos.CredencialResponse;
import com.promou.seguridad.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HU 1.4.1 Generar identificacion digital. */
@RestController
@RequestMapping("/api/estudiantes/me")
public class CredencialController {

    private final CredencialService credenciales;

    public CredencialController(CredencialService credenciales) {
        this.credenciales = credenciales;
    }

    /** HU 1.4.1 */
    @GetMapping("/credencial")
    public CredencialResponse credencial(@AuthenticationPrincipal UsuarioAutenticado u) {
        return credenciales.obtenerPara(u.usuarioId());
    }
}
