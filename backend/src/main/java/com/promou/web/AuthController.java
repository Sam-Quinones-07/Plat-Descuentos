package com.promou.web;

import com.promou.aplicacion.AutenticacionService;
import com.promou.aplicacion.RegistroService;
import com.promou.aplicacion.dto.AuthDtos.LoginRequest;
import com.promou.aplicacion.dto.AuthDtos.LoginResponse;
import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.dto.AuthDtos.PerfilDto;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstablecimientoRequest;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstudianteRequest;
import com.promou.dominio.usuario.Rol;
import com.promou.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU 1.1.1, 1.1.3, 1.1.4, 1.2.1, 1.2.4, 1.2.5. Solo traduce HTTP <-> casos de uso. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistroService registro;
    private final AutenticacionService autenticacion;

    public AuthController(RegistroService registro, AutenticacionService autenticacion) {
        this.registro = registro;
        this.autenticacion = autenticacion;
    }

    @PostMapping("/estudiantes/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse registrarEstudiante(@Valid @RequestBody RegistroEstudianteRequest r) {
        return registro.registrarEstudiante(r);
    }

    @PostMapping("/establecimientos/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse registrarEstablecimiento(@Valid @RequestBody RegistroEstablecimientoRequest r) {
        return registro.registrarEstablecimiento(r);
    }

    /** Login de estudiantes y del administrador (el front usa la misma pantalla para ambos). */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        return autenticacion.iniciarSesion(r.email(), r.password(), Set.of(Rol.ESTUDIANTE, Rol.ADMINISTRADOR));
    }

    @PostMapping("/establecimientos/login")
    public LoginResponse loginEstablecimiento(@Valid @RequestBody LoginRequest r) {
        return autenticacion.iniciarSesion(r.email(), r.password(), Set.of(Rol.ESTABLECIMIENTO));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UsuarioAutenticado actual) {
        autenticacion.cerrarSesion(actual);
    }

    /** Para restaurar la sesión al recargar la página. */
    @GetMapping("/me")
    public PerfilDto yo(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return autenticacion.perfil(actual);
    }
}
