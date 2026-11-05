package com.promou.seguridad;

import com.promou.comun.CodigosSeguros;
import com.promou.config.PromoUProperties;
import com.promou.dominio.cuenta.Sesion;
import com.promou.dominio.cuenta.SesionRepository;
import com.promou.dominio.usuario.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sesiones del lado del servidor con token opaco: se pueden revocar y expiran por inactividad (30 min). */
@Service
public class SesionService {

    public enum Estado { ACTIVA, EXPIRADA, INEXISTENTE }

    public record ResultadoSesion(Estado estado, UsuarioAutenticado principal) {
    }

    private final SesionRepository repo;
    private final PromoUProperties props;
    private final Clock reloj;

    public SesionService(SesionRepository repo, PromoUProperties props, Clock reloj) {
        this.repo = repo;
        this.props = props;
        this.reloj = reloj;
    }

    /** @return el token en claro; en BD solo queda su hash. */
    @Transactional
    public String crear(Usuario usuario) {
        String token = CodigosSeguros.tokenOpaco();
        repo.save(new Sesion(CodigosSeguros.sha256Hex(token), usuario.getId(), usuario.getRol(), reloj.instant()));
        return token;
    }

    @Transactional
    public ResultadoSesion autenticar(String token) {
        Optional<Sesion> encontrada = repo.findByTokenHash(CodigosSeguros.sha256Hex(token));
        if (encontrada.isEmpty()) {
            return new ResultadoSesion(Estado.INEXISTENTE, null);
        }
        Sesion sesion = encontrada.get();
        Instant ahora = reloj.instant();
        if (sesion.expiro(ahora, props.seguridad().inactividadSesion())) {
            repo.delete(sesion);
            return new ResultadoSesion(Estado.EXPIRADA, null);
        }
        sesion.registrarActividad(ahora);
        return new ResultadoSesion(Estado.ACTIVA,
                new UsuarioAutenticado(sesion.getUsuarioId(), sesion.getId(), sesion.getRol()));
    }

    /** HU 1.1.4 / 1.2.5: la sesión se elimina del servidor; el token deja de servir. */
    @Transactional
    public void cerrar(UUID sesionId) {
        repo.deleteById(sesionId);
    }

    @Transactional
    public void cerrarOtras(UUID usuarioId, UUID conservar) {
        repo.cerrarOtras(usuarioId, conservar);
    }

    @Transactional
    public void cerrarTodas(UUID usuarioId) {
        repo.cerrarTodas(usuarioId);
    }
}
