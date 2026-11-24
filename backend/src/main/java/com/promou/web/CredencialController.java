package com.promou.web;

import com.promou.aplicacion.CredencialService;
import com.promou.aplicacion.dto.DescuentoDtos.CredencialResponse;
import com.promou.aplicacion.dto.DescuentoDtos.ValidacionQrRequest;
import com.promou.aplicacion.dto.DescuentoDtos.ValidacionQrResponse;
import com.promou.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** HU 1.4.1 (el estudiante ve su identificación) y HU 1.4.2 (el comercio la valida). */
@RestController
public class CredencialController {

    private final CredencialService credenciales;

    public CredencialController(CredencialService credenciales) {
        this.credenciales = credenciales;
    }

    @GetMapping("/api/estudiantes/me/credencial")
    public CredencialResponse credencial(@AuthenticationPrincipal UsuarioAutenticado u) {
        return credenciales.obtenerPara(u.usuarioId());
    }

    @PostMapping("/api/establecimientos/me/validar-credencial")
    public ValidacionQrResponse validar(@AuthenticationPrincipal UsuarioAutenticado u,
                                        @Valid @RequestBody ValidacionQrRequest r) {
        return credenciales.validarQr(u.usuarioId(), r.code());
    }
}
