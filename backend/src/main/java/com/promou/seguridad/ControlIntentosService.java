package com.promou.seguridad;

import com.promou.comun.CodigosSeguros;
import com.promou.config.PromoUProperties;
import com.promou.dominio.cuenta.IntentoLogin;
import com.promou.dominio.cuenta.IntentoLoginRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bloqueo temporal por intentos fallidos (HU 1.1.3 esc. 6). Se lleva por correo exista o no la cuenta,
 * para que el mensaje de bloqueo no delate qué correos están registrados.
 * Ningún método lanza excepciones de negocio: así no marca como "rollback-only" la transacción del login.
 */
@Service
public class ControlIntentosService {

    private final IntentoLoginRepository repo;
    private final PromoUProperties props;
    private final Clock reloj;

    public ControlIntentosService(IntentoLoginRepository repo, PromoUProperties props, Clock reloj) {
        this.repo = repo;
        this.props = props;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public boolean estaBloqueado(String correoNormalizado) {
        Instant ahora = reloj.instant();
        return repo.findById(clave(correoNormalizado)).map(i -> i.bloqueado(ahora)).orElse(false);
    }

    /** @return true si este fallo activa el bloqueo. */
    @Transactional
    public boolean registrarFallo(String correoNormalizado) {
        String clave = clave(correoNormalizado);
        IntentoLogin intento = repo.buscarParaActualizar(clave).orElseGet(() -> repo.save(new IntentoLogin(clave)));
        PromoUProperties.Seguridad s = props.seguridad();
        return intento.registrarFallo(reloj.instant(), s.maxIntentosFallidos(), s.ventanaIntentos(), s.duracionBloqueo());
    }

    @Transactional
    public void reiniciar(String correoNormalizado) {
        repo.findById(clave(correoNormalizado)).ifPresent(repo::delete);
    }

    private static String clave(String correoNormalizado) {
        return CodigosSeguros.sha256Hex(correoNormalizado);
    }
}
