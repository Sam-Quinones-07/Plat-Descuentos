package com.promou.aplicacion;

import com.promou.aplicacion.eventos.CorreoPendiente;
import com.promou.aplicacion.notificacion.NotificacionService.Mensaje;
import com.promou.comun.CodigosSeguros;
import com.promou.comun.LimitadorPeticiones;
import com.promou.config.PromoUProperties;
import com.promou.dominio.cuenta.TokenVerificacion;
import com.promou.dominio.cuenta.TokenVerificacionRepository;
import com.promou.dominio.usuario.Usuario;
import java.time.Clock;
import java.time.Duration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Emite enlaces de un solo uso y deja el correo listo para salir después del commit. Lo comparten la confirmación
 * de registro (HU 1.1.2, 1.2.2) y el restablecimiento de contraseña (HU 1.3.2), para no duplicar la lógica.
 */
@Component
public class EnlacesCorreo {

    private final TokenVerificacionRepository tokens;
    private final LimitadorPeticiones limiteCorreos;
    private final ApplicationEventPublisher eventos;
    private final PromoUProperties props;
    private final Clock reloj;

    public EnlacesCorreo(TokenVerificacionRepository tokens, LimitadorPeticiones limiteCorreos,
                         ApplicationEventPublisher eventos, PromoUProperties props, Clock reloj) {
        this.tokens = tokens;
        this.limiteCorreos = limiteCorreos;
        this.eventos = eventos;
        this.props = props;
        this.reloj = reloj;
    }

    /** Límite por destinatario: evita usar la plataforma para llenar de correos el buzón de alguien. */
    public boolean puedeEnviarA(String correo) {
        return limiteCorreos.permitir(CodigosSeguros.sha256Hex(correo), reloj.instant());
    }

    /**
     * Anula los enlaces anteriores del mismo tipo (el viejo deja de servir) y programa el envío del nuevo.
     * @param ruta ruta del front, p. ej. "confirmar" -> https://front/#confirmar/{token}
     */
    public void emitir(Usuario usuario, TokenVerificacion.Tipo tipo, Duration vigencia, String ruta, String asunto,
                       String plantilla) {
        tokens.anularAnteriores(usuario.getId(), tipo);
        String token = CodigosSeguros.tokenOpaco();
        tokens.save(TokenVerificacion.emitir(usuario, tipo, CodigosSeguros.sha256Hex(token), reloj.instant(), vigencia));
        String enlace = props.front().url().replaceAll("/+$", "") + "/#" + ruta + "/" + token;
        String cuerpo = plantilla.replace("{nombre}", usuario.getNombreVisible()).replace("{enlace}", enlace);
        eventos.publishEvent(new CorreoPendiente(new Mensaje(usuario.getCorreo(), asunto, cuerpo)));
    }
}
