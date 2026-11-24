package com.promou.web;

import com.promou.aplicacion.AutenticacionService;
import com.promou.aplicacion.AutenticacionService.SesionIniciada;
import com.promou.aplicacion.ConfirmacionCorreoService;
import com.promou.aplicacion.CuentaService;
import com.promou.aplicacion.RecuperacionContrasenaService;
import com.promou.aplicacion.RegistroService;
import com.promou.aplicacion.dto.AuthDtos.CambioContrasenaRequest;
import com.promou.aplicacion.dto.AuthDtos.ConfirmacionRequest;
import com.promou.aplicacion.dto.AuthDtos.ConfirmacionResponse;
import com.promou.aplicacion.dto.AuthDtos.CorreoRequest;
import com.promou.aplicacion.dto.AuthDtos.LoginRequest;
import com.promou.aplicacion.dto.AuthDtos.LoginResponse;
import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.dto.AuthDtos.PerfilDto;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstablecimientoRequest;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstudianteRequest;
import com.promou.aplicacion.dto.AuthDtos.RestablecerRequest;
import com.promou.dominio.usuario.Rol;
import com.promou.seguridad.CookieSesion;
import com.promou.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU 1.1.1 a 1.1.4, 1.2.1, 1.2.2, 1.2.4, 1.2.5, 1.3.1 y 1.3.2. Solo traduce HTTP <-> casos de uso. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistroService registro;
    private final AutenticacionService autenticacion;
    private final ConfirmacionCorreoService confirmacion;
    private final RecuperacionContrasenaService recuperacion;
    private final CuentaService cuenta;
    private final CookieSesion cookie;

    public AuthController(RegistroService registro, AutenticacionService autenticacion,
                          ConfirmacionCorreoService confirmacion, RecuperacionContrasenaService recuperacion,
                          CuentaService cuenta, CookieSesion cookie) {
        this.registro = registro;
        this.autenticacion = autenticacion;
        this.confirmacion = confirmacion;
        this.recuperacion = recuperacion;
        this.cuenta = cuenta;
        this.cookie = cookie;
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

    /** HU 1.1.2 / 1.2.2: el front recibe el token del enlace (#confirmar/{token}) y lo envía aquí. */
    @PostMapping("/confirmar")
    public ConfirmacionResponse confirmar(@Valid @RequestBody ConfirmacionRequest r) {
        return confirmacion.confirmar(r.token());
    }

    @PostMapping("/confirmacion/reenviar")
    public MensajeResponse reenviarConfirmacion(@Valid @RequestBody CorreoRequest r) {
        return confirmacion.reenviar(r.email());
    }

    /** HU 1.3.2 */
    @PostMapping("/contrasena/recuperar")
    public MensajeResponse recuperar(@Valid @RequestBody CorreoRequest r) {
        return recuperacion.solicitar(r.email());
    }

    @GetMapping("/contrasena/restablecer/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verificarEnlace(@PathVariable String token) {
        recuperacion.verificarEnlace(token);
    }

    @PostMapping("/contrasena/restablecer")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerRequest r) {
        return recuperacion.restablecer(r.token(), r.password(), r.confirmation());
    }

    /** HU 1.3.1 */
    @PostMapping("/contrasena")
    public MensajeResponse cambiarContrasena(@AuthenticationPrincipal UsuarioAutenticado actual,
                                             @Valid @RequestBody CambioContrasenaRequest r) {
        return cuenta.cambiarContrasena(actual, r);
    }

    /** Login de estudiantes y del administrador (el front usa la misma pantalla para ambos). */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest r) {
        return conCookie(autenticacion.iniciarSesion(r.email(), r.password(),
                Set.of(Rol.ESTUDIANTE, Rol.ADMINISTRADOR)));
    }

    @PostMapping("/establecimientos/login")
    public ResponseEntity<LoginResponse> loginEstablecimiento(@Valid @RequestBody LoginRequest r) {
        return conCookie(autenticacion.iniciarSesion(r.email(), r.password(), Set.of(Rol.ESTABLECIMIENTO)));
    }

    /** HU 1.1.4 / 1.2.5: borra la sesión en el servidor y la cookie en el navegador. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioAutenticado actual) {
        autenticacion.cerrarSesion(actual);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.borrar()).build();
    }

    /** Para restaurar la sesión al recargar la página. */
    @GetMapping("/me")
    public PerfilDto yo(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return autenticacion.perfil(actual);
    }

    private ResponseEntity<LoginResponse> conCookie(SesionIniciada sesion) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.crear(sesion.token())).body(sesion.respuesta());
    }
}
