package com.promou.aplicacion;

import com.promou.aplicacion.dto.DescuentoDtos.ResenaModeracion;
import com.promou.aplicacion.dto.DescuentoDtos.ResenaResponse;
import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoRepository;
import com.promou.dominio.resena.Resena;
import com.promou.dominio.resena.ResenaRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.EstudianteRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 3.1.1 (visualizar reseñas) y 3.1.2 (escribir reseña). Una reseña por estudiante y descuento; el administrador
 * puede retirarlas (deuda técnica 10: todavía no se puede comprobar que el estudiante usó el descuento).
 */
@Service
public class ResenaService {

    private final ResenaRepository resenas;
    private final DescuentoRepository descuentos;
    private final EstudianteRepository estudiantes;
    private final Clock reloj;

    public ResenaService(ResenaRepository resenas, DescuentoRepository descuentos, EstudianteRepository estudiantes,
                         Clock reloj) {
        this.resenas = resenas;
        this.descuentos = descuentos;
        this.estudiantes = estudiantes;
        this.reloj = reloj;
    }

    /** Esc. 1 y 2 de la HU 3.1.1: lista vacía = "aún no hay reseñas". */
    @Transactional(readOnly = true)
    public List<ResenaResponse> listar(UUID descuentoId, UUID estudianteActual) {
        return resenas.publicadas(descuentoId).stream().map(r -> Mapeos.resena(r, estudianteActual)).toList();
    }

    /** HU 3.1.2 esc. 2 y 3. */
    @Transactional
    public ResenaResponse publicar(UUID estudianteId, UUID descuentoId, String texto) {
        Descuento d = descuentos.findById(descuentoId).orElseThrow(
                () -> ReglaNegocioException.noEncontrado("DESCUENTO_NO_ENCONTRADO", "El descuento no existe."));
        Estudiante e = estudiantes.findById(estudianteId).orElseThrow(
                () -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
        Resena r = new Resena(e, d, texto, reloj.instant());
        r.validar();
        if (resenas.existePor(estudianteId, descuentoId)) {
            throw ReglaNegocioException.conflicto("RESENA_YA_PUBLICADA", "Ya publicaste una reseña para este descuento.");
        }
        return Mapeos.resena(resenas.save(r), estudianteId);
    }

    @Transactional(readOnly = true)
    public List<ResenaModeracion> recientes() {
        return resenas.recientes(PageRequest.of(0, 100)).stream()
                .map(r -> new ResenaModeracion(r.getId(), r.getEstudiante().getNombreCorto(),
                        r.getDescuento().getEstablecimiento().getNombreComercial(), r.getDescuento().getBeneficio(),
                        r.getTexto(), r.getPublicadaEn(), r.isRetirada()))
                .toList();
    }

    @Transactional
    public void retirar(UUID resenaId) {
        resenas.findById(resenaId).orElseThrow(
                () -> ReglaNegocioException.noEncontrado("RESENA_NO_ENCONTRADA", "La reseña no existe.")).retirar();
    }
}
