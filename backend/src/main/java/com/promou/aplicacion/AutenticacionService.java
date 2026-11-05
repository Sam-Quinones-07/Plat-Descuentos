package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.LoginResponse;
import com.promou.aplicacion.dto.AuthDtos.PerfilDto;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.config.PromoUProperties;
import com.promou.dominio.usuario.Rol;
import com.promou.dominio.usuario.Usuario;
import com.promou.dominio.usuario.UsuarioRepository;
import com.promou.seguridad.ControlIntentosService;
import com.promou.seguridad.SesionService;
import com.promou.seguridad.UsuarioAutenticado;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 1.1.3 / 1.2.4 (iniciar sesión), 1.1.4 / 1.2.5 (cerrar sesión). */
@Service
public class AutenticacionService {

    private static final String MSG_CREDENCIALES = "Las credenciales ingresadas no son correctas.";
    private static final String MSG_BLOQUEO = "El acceso está temporalmente bloqueado. Inténtalo de nuevo más tarde.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final ControlIntentosService intentos;
    private final SesionService sesiones;
    private final PromoUProperties props;
    /** Se compara contra este hash cuando el correo no existe, para que el tiempo de respuesta no delate cuentas. */
    private final String hashFicticio;

    public AutenticacionService(UsuarioRepository usuarios, PasswordEncoder encoder, ControlIntentosService intentos,
                                SesionService sesiones, PromoUProperties props) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.intentos = intentos;
        this.sesiones = sesiones;
        this.props = props;
        this.hashFicticio = encoder.encode(java.util.UUID.randomUUID().toString());
    }

    /**
     * noRollbackFor: los intentos fallidos deben quedar guardados aunque el login termine en excepción.
     * Los mensajes de credenciales son genéricos (no dicen si falló el correo o la contraseña).
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public LoginResponse iniciarSesion(String email, String contrasena, Set<Rol> rolesPermitidos) {
        String correo = Texto.normalizarCorreo(email);
        if (intentos.estaBloqueado(correo)) {
            throw ReglaNegocioException.bloqueado("CUENTA_BLOQUEADA", MSG_BLOQUEO);
        }
        Usuario usuario = usuarios.findByCorreo(correo).filter(u -> rolesPermitidos.contains(u.getRol())).orElse(null);
        boolean coincide = encoder.matches(contrasena, usuario != null ? usuario.getContrasenaHash() : hashFicticio);
        if (usuario == null || !coincide) {
            if (intentos.registrarFallo(correo)) {
                throw ReglaNegocioException.bloqueado("CUENTA_BLOQUEADA", MSG_BLOQUEO);
            }
            throw ReglaNegocioException.noAutenticado("CREDENCIALES_INCORRECTAS", MSG_CREDENCIALES);
        }
        intentos.reiniciar(correo);
        usuario.verificarPuedeIniciarSesion(); // correo confirmado y, si aplica, afiliación aprobada
        String token = sesiones.crear(usuario);
        return new LoginResponse(token, props.seguridad().inactividadSesion().toSeconds(), Mapeos.perfil(usuario));
    }

    @Transactional(readOnly = true)
    public PerfilDto perfil(UsuarioAutenticado actual) {
        return usuarios.findById(actual.usuarioId()).map(Mapeos::perfil)
                .orElseThrow(() -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
    }

    public void cerrarSesion(UsuarioAutenticado actual) {
        sesiones.cerrar(actual.sesionId());
    }
}
