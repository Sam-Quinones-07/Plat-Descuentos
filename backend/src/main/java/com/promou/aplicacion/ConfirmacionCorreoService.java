package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.ConfirmacionResponse;
import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.eventos.EstudianteVerificado;
import com.promou.comun.CodigosSeguros;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.config.PromoUProperties;
import com.promou.dominio.cuenta.TokenVerificacion;
import com.promou.dominio.cuenta.TokenVerificacionRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.Usuario;
import com.promou.dominio.usuario.UsuarioRepository;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 1.1.2 y 1.2.2: confirmar el registro con un enlace enviado al correo. */
@Service
public class ConfirmacionCorreoService {

    static final String MENSAJE_REENVIO =
            "Si el correo corresponde a una cuenta sin confirmar, te enviamos un nuevo enlace de confirmación.";
    private static final String ASUNTO = "PromoU: confirma tu correo electrónico";
    private static final String PLANTILLA = """
            Hola, {nombre}.

            Para confirmar tu registro en PromoU abre este enlace (vence en 24 horas):
            {enlace}

            Si no te registraste en PromoU, ignora este mensaje.

            — Equipo PromoU""";

    private final TokenVerificacionRepository tokens;
    private final UsuarioRepository usuarios;
    private final EnlacesCorreo enlaces;
    private final ApplicationEventPublisher eventos;
    private final PromoUProperties props;
    private final Clock reloj;

    public ConfirmacionCorreoService(TokenVerificacionRepository tokens, UsuarioRepository usuarios,
                                     EnlacesCorreo enlaces, ApplicationEventPublisher eventos,
                                     PromoUProperties props, Clock reloj) {
        this.tokens = tokens;
        this.usuarios = usuarios;
        this.enlaces = enlaces;
        this.eventos = eventos;
        this.props = props;
        this.reloj = reloj;
    }

    /** Esc. 1: lo llama el registro, dentro de su transacción. El correo sale después del commit. */
    public void enviarEnlace(Usuario usuario) {
        if (enlaces.puedeEnviarA(usuario.getCorreo())) {
            enlaces.emitir(usuario, TokenVerificacion.Tipo.CONFIRMACION,
                    props.seguridad().vigenciaEnlaceConfirmacion(), "confirmar", ASUNTO, PLANTILLA);
        }
    }

    /**
     * Esc. 2, 3 y 4 (HU 1.2.2 esc. 4: enlace desconocido = mensaje genérico). noRollbackFor no aplica: si el enlace
     * no sirve no hay nada que guardar.
     */
    @Transactional
    public ConfirmacionResponse confirmar(String token) {
        TokenVerificacion t = tokens.buscar(CodigosSeguros.sha256Hex(token.trim()), TokenVerificacion.Tipo.CONFIRMACION)
                .orElseThrow(() -> ReglaNegocioException.validacion("ENLACE_INVALIDO",
                        "No es posible completar la confirmación. Solicita un nuevo enlace."));
        t.confirmar(reloj.instant());
        Usuario usuario = t.getUsuario();
        if (usuario instanceof Estudiante e) {
            // Observer: confirmar el correo institucional verifica al estudiante y emite su identificación (HU 1.4.1)
            eventos.publishEvent(new EstudianteVerificado(e.getId()));
        }
        return new ConfirmacionResponse("Tu registro fue confirmado exitosamente. Ya puedes iniciar sesión.",
                usuario.getRol().etiquetaFront());
    }

    /** HU 1.2.2 esc. 5 (y también para estudiantes). La respuesta es la misma exista o no la cuenta. */
    @Transactional
    public MensajeResponse reenviar(String email) {
        String correo = Texto.normalizarCorreo(email);
        usuarios.findByCorreo(correo)
                .filter(u -> !u.isCorreoConfirmado())
                .ifPresent(this::enviarEnlace);
        return new MensajeResponse(MENSAJE_REENVIO);
    }
}
