package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.CambioContrasenaRequest;
import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.usuario.Usuario;
import com.promou.dominio.usuario.UsuarioRepository;
import com.promou.seguridad.SesionService;
import com.promou.seguridad.UsuarioAutenticado;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 1.3.1: cambiar la contraseña desde el perfil. */
@Service
public class CuentaService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final SesionService sesiones;

    public CuentaService(UsuarioRepository usuarios, PasswordEncoder encoder, SesionService sesiones) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.sesiones = sesiones;
    }

    /** Esc. 2 a 4. Las demás sesiones abiertas se cierran; la actual sigue activa. */
    @Transactional
    public MensajeResponse cambiarContrasena(UsuarioAutenticado actual, CambioContrasenaRequest r) {
        if (r.confirmation() != null && !r.confirmation().equals(r.newPassword())) {
            throw ReglaNegocioException.validacion("DATOS_INVALIDOS", "Las contraseñas no coinciden.",
                    Map.of("confirmation", "Las contraseñas no coinciden."));
        }
        Usuario usuario = usuarios.findById(actual.usuarioId()).orElseThrow(
                () -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
        usuario.cambiarContrasena(r.currentPassword(), r.newPassword(), encoder);
        sesiones.cerrarOtras(usuario.getId(), actual.sesionId());
        return new MensajeResponse("Tu contraseña se cambió correctamente.");
    }
}
