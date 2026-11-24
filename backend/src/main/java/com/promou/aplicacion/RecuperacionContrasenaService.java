package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.comun.CodigosSeguros;
import com.promou.comun.PoliticaContrasena;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.config.PromoUProperties;
import com.promou.dominio.cuenta.TokenVerificacion;
import com.promou.dominio.cuenta.TokenVerificacionRepository;
import com.promou.dominio.usuario.Usuario;
import com.promou.dominio.usuario.UsuarioRepository;
import com.promou.seguridad.ControlIntentosService;
import com.promou.seguridad.SesionService;
import java.time.Clock;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 1.3.2: restablecer la contraseña con un enlace de un solo uso. */
@Service
public class RecuperacionContrasenaService {

    static final String MENSAJE_SOLICITUD =
            "Si la información corresponde a una cuenta registrada, recibirás un correo para continuar con el proceso.";
    private static final String MENSAJE_ENLACE = "No es posible completar el proceso. Solicita un nuevo enlace.";
    private static final String ASUNTO = "PromoU: restablece tu contraseña";
    private static final String PLANTILLA = """
            Hola, {nombre}.

            Recibimos una solicitud para restablecer tu contraseña de PromoU. Abre este enlace (vence en 1 hora):
            {enlace}

            Si no fuiste tú, ignora este mensaje: tu contraseña no cambiará.

            — Equipo PromoU""";

    private final UsuarioRepository usuarios;
    private final TokenVerificacionRepository tokens;
    private final EnlacesCorreo enlaces;
    private final SesionService sesiones;
    private final ControlIntentosService intentos;
    private final PasswordEncoder encoder;
    private final PromoUProperties props;
    private final Clock reloj;

    public RecuperacionContrasenaService(UsuarioRepository usuarios, TokenVerificacionRepository tokens,
                                         EnlacesCorreo enlaces, SesionService sesiones,
                                         ControlIntentosService intentos, PasswordEncoder encoder,
                                         PromoUProperties props, Clock reloj) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.enlaces = enlaces;
        this.sesiones = sesiones;
        this.intentos = intentos;
        this.encoder = encoder;
        this.props = props;
        this.reloj = reloj;
    }

    /** Esc. 1: la misma respuesta exista o no el correo, para no revelar qué cuentas están registradas. */
    @Transactional
    public MensajeResponse solicitar(String email) {
        String correo = Texto.normalizarCorreo(email);
        usuarios.findByCorreo(correo).filter(u -> enlaces.puedeEnviarA(correo)).ifPresent(u ->
                enlaces.emitir(u, TokenVerificacion.Tipo.RESTABLECIMIENTO,
                        props.seguridad().vigenciaEnlaceRestablecimiento(), "restablecer", ASUNTO, PLANTILLA));
        return new MensajeResponse(MENSAJE_SOLICITUD);
    }

    /** Esc. 2: al abrir el enlace se comprueba que siga vigente antes de mostrar el formulario. */
    @Transactional(readOnly = true)
    public void verificarEnlace(String token) {
        boolean vigente = tokens.buscar(CodigosSeguros.sha256Hex(token.trim()), TokenVerificacion.Tipo.RESTABLECIMIENTO)
                .map(t -> t.vigente(reloj.instant())).orElse(false);
        if (!vigente) {
            throw ReglaNegocioException.validacion("ENLACE_INVALIDO", MENSAJE_ENLACE);
        }
    }

    /**
     * Esc. 3 a 7. La contraseña se valida antes de consumir el enlace, así un error de requisitos no lo gasta.
     * Al terminar se cierran todas las sesiones: la contraseña anterior y quien la tuviera quedan fuera.
     */
    @Transactional
    public MensajeResponse restablecer(String token, String nueva, String confirmacion) {
        PoliticaContrasena.exigir(nueva, "password");
        if (confirmacion != null && !confirmacion.equals(nueva)) {
            throw ReglaNegocioException.validacion("DATOS_INVALIDOS", "Las contraseñas no coinciden.",
                    Map.of("confirmation", "Las contraseñas no coinciden."));
        }
        TokenVerificacion t = tokens.buscar(CodigosSeguros.sha256Hex(token.trim()), TokenVerificacion.Tipo.RESTABLECIMIENTO)
                .orElseThrow(() -> ReglaNegocioException.validacion("ENLACE_INVALIDO", MENSAJE_ENLACE));
        t.consumir(reloj.instant());
        Usuario usuario = t.getUsuario();
        usuario.restablecerContrasena(nueva, encoder);
        sesiones.cerrarTodas(usuario.getId());
        intentos.reiniciar(usuario.getCorreo());
        return new MensajeResponse("Tu contraseña fue restablecida correctamente. Ya puedes iniciar sesión.");
    }
}
