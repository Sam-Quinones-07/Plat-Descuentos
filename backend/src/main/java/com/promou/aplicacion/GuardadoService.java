package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoRepository;
import com.promou.dominio.guardado.DescuentoGuardado;
import com.promou.dominio.guardado.GuardadoRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.EstudianteRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU 2.2.4: guardar, consultar y dejar de guardar descuentos. */
@Service
public class GuardadoService {

    private final GuardadoRepository guardados;
    private final DescuentoRepository descuentos;
    private final EstudianteRepository estudiantes;
    private final Clock reloj;

    public GuardadoService(GuardadoRepository guardados, DescuentoRepository descuentos,
                           EstudianteRepository estudiantes, Clock reloj) {
        this.guardados = guardados;
        this.descuentos = descuentos;
        this.estudiantes = estudiantes;
        this.reloj = reloj;
    }

    /** Esc. 1 y 4: solo un descuento disponible, y sin duplicados (también lo impide la restricción única). */
    @Transactional
    public MensajeResponse guardar(UUID estudianteId, UUID descuentoId) {
        Descuento d = descuentos.findById(descuentoId).orElseThrow(
                () -> ReglaNegocioException.noEncontrado("DESCUENTO_NO_ENCONTRADO", "El descuento no existe."));
        if (!d.estaDisponible(LocalDate.now(reloj))) {
            throw ReglaNegocioException.noDisponible("DESCUENTO_NO_DISPONIBLE", "El descuento ya no está disponible.");
        }
        if (guardados.existePor(estudianteId, descuentoId)) {
            throw ReglaNegocioException.conflicto("DESCUENTO_YA_GUARDADO", "Este descuento ya está en tus guardados.");
        }
        Estudiante e = estudiantes.getReferenceById(estudianteId);
        guardados.save(new DescuentoGuardado(e, d, reloj.instant()));
        return new MensajeResponse("Descuento guardado.");
    }

    /** Esc. 3: el descuento sigue en el directorio si está vigente; solo se quita de los guardados. */
    @Transactional
    public void quitar(UUID estudianteId, UUID descuentoId) {
        guardados.buscar(estudianteId, descuentoId).ifPresent(guardados::delete);
    }

    /** Esc. 2: incluye los que ya vencieron (available = false), para que el estudiante sepa que no los puede usar. */
    @Transactional(readOnly = true)
    public List<DescuentoResponse> listar(UUID estudianteId) {
        LocalDate hoy = LocalDate.now(reloj);
        return guardados.delEstudiante(estudianteId).stream()
                .map(g -> Mapeos.descuento(g.getDescuento(), hoy, false).conGuardado(true))
                .toList();
    }
}
