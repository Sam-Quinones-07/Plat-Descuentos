package com.promou.aplicacion;

import com.promou.aplicacion.dto.DescuentoDtos.CredencialResponse;
import com.promou.aplicacion.dto.DescuentoDtos.ValidacionQrResponse;
import com.promou.aplicacion.eventos.EstudianteVerificado;
import com.promou.comun.CodigosSeguros;
import com.promou.comun.ReglaNegocioException;
import com.promou.config.PromoUProperties;
import com.promou.dominio.credencial.CredencialDigital;
import com.promou.dominio.credencial.CredencialRepository;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.EstablecimientoRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.EstudianteRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 1.4.1 (generar y mostrar la identificación digital) y HU 1.4.2 (validarla desde el comercio). */
@Service
public class CredencialService {

    private final CredencialRepository credenciales;
    private final EstudianteRepository estudiantes;
    private final EstablecimientoRepository establecimientos;
    private final PromoUProperties props;
    private final Clock reloj;

    public CredencialService(CredencialRepository credenciales, EstudianteRepository estudiantes,
                             EstablecimientoRepository establecimientos, PromoUProperties props, Clock reloj) {
        this.credenciales = credenciales;
        this.estudiantes = estudiantes;
        this.establecimientos = establecimientos;
        this.props = props;
        this.reloj = reloj;
    }

    /**
     * HU 1.4.2: solo un establecimiento aprobado valida. Responde lo mínimo (Ley 1581): nombre corto, universidad y
     * vencimiento, sin correo ni documento. Esc. 1 VALIDA, esc. 2 NO_VIGENTE, esc. 3 NO_RECONOCIDA.
     */
    @Transactional(readOnly = true)
    public ValidacionQrResponse validarQr(java.util.UUID establecimientoId, String codigo) {
        Establecimiento comercio = establecimientos.findById(establecimientoId).orElseThrow(
                () -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
        comercio.exigirOperativo();
        String limpio = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        return credenciales.buscarPorCodigo(limpio).map(c -> {
            Estudiante e = c.getEstudiante();
            if (!c.validar(reloj.instant())) {
                return new ValidacionQrResponse("NO_VIGENTE",
                        "La identificación no está vigente. No permite acceder al descuento.", null, null,
                        c.getFechaVencimiento());
            }
            return new ValidacionQrResponse("VALIDA", "Identificación válida: es un estudiante vigente.",
                    e.getNombreCorto(), e.getUniversidad(), c.getFechaVencimiento());
        }).orElseGet(() -> new ValidacionQrResponse("NO_RECONOCIDA",
                "No se pudo validar la identificación: el código no corresponde a ninguna registrada.", null, null,
                null));
    }

    /** Observer: al verificarse el estudiante se emite su identificación (esc. 1). Corre en la misma transacción. */
    @EventListener
    public void alVerificarse(EstudianteVerificado evento) {
        estudiantes.findById(evento.estudianteId()).ifPresent(this::obtenerOEmitir);
    }

    /** Esc. 2, 3 y 4: muestra la existente; solo genera una si no hay y el estudiante está verificado. */
    @Transactional
    public CredencialResponse obtenerPara(UUID estudianteId) {
        Estudiante estudiante = estudiantes.findById(estudianteId).orElseThrow(
                () -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
        if (!estudiante.isEstadoVerificado()) {
            throw ReglaNegocioException.prohibido("ESTUDIANTE_NO_VERIFICADO",
                    "Debes completar la verificación de tu condición de estudiante.");
        }
        CredencialDigital c = obtenerOEmitir(estudiante);
        Instant ahora = reloj.instant();
        return new CredencialResponse(c.getCodigoQr(), estudiante.getNombre(), estudiante.getApellido(),
                estudiante.getUniversidad(), true, c.validar(ahora), c.getFechaEmision(), c.getFechaVencimiento());
    }

    private CredencialDigital obtenerOEmitir(Estudiante estudiante) {
        // Esc. 4: si ya existe se reutiliza; si no, el estudiante la crea (Creator) y se persiste
        return credenciales.ultimaActiva(estudiante.getId()).orElseGet(() -> credenciales.save(
                estudiante.generarCredencial(CodigosSeguros.codigoCredencial(), reloj.instant(),
                        props.seguridad().vigenciaCredencial())));
    }
}
