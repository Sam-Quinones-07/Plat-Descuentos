package com.promou.aplicacion;

import com.promou.aplicacion.dto.DescuentoDtos.DescuentoRequest;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.dominio.descuento.Categoria;
import com.promou.dominio.descuento.CategoriaRepository;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoDatos;
import com.promou.dominio.descuento.DescuentoRepository;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.EstablecimientoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 2.1.1 Publicar descuento. GRASP Controller del caso de uso: coordina, no decide.
 * - Creator: {@link Establecimiento#crearDescuento} crea el descuento (con Builder).
 * - Information Expert: {@link Descuento#validar()} valida sus propios datos.
 * - Pure Fabrication / Indirection: {@link DescuentoRepository} lo persiste.
 * El controlador REST solo traduce HTTP; no contiene reglas (Low Coupling, High Cohesion).
 */
@Service
public class GestionDescuentosService {

    private final EstablecimientoRepository establecimientos;
    private final DescuentoRepository descuentos;
    private final CategoriaRepository categorias;
    private final Clock reloj;

    public GestionDescuentosService(EstablecimientoRepository establecimientos, DescuentoRepository descuentos,
                                    CategoriaRepository categorias, Clock reloj) {
        this.establecimientos = establecimientos;
        this.descuentos = descuentos;
        this.categorias = categorias;
        this.reloj = reloj;
    }

    @Transactional
    public DescuentoResponse publicar(UUID establecimientoId, DescuentoRequest r) {
        Establecimiento establecimiento = cargarOperativo(establecimientoId);
        Descuento descuento = establecimiento.crearDescuento(aDatos(r), resolverCategoria(r.category()));
        descuento.validar();
        descuento.registrarFechas(reloj.instant());
        establecimiento.agregarDescuento(descuento);
        descuentos.save(descuento);
        return respuesta(descuento);
    }

    /** Tablero "Mis descuentos": muestra lo publicado (edición y desactivación llegan en Release 2). */
    @Transactional(readOnly = true)
    public List<DescuentoResponse> listarPropios(UUID establecimientoId) {
        cargarOperativo(establecimientoId);
        return descuentos.delEstablecimiento(establecimientoId).stream().map(this::respuesta).toList();
    }

    private Establecimiento cargarOperativo(UUID id) {
        Establecimiento e = establecimientos.findById(id).orElseThrow(
                () -> ReglaNegocioException.noAutenticado("NO_AUTENTICADO", "Debes iniciar sesión."));
        e.exigirOperativo();
        return e;
    }

    private Categoria resolverCategoria(String id) {
        String limpio = Texto.nuloSiVacio(id);
        if (limpio == null) {
            return null; // Descuento.validar() informa "La categoría es obligatoria."
        }
        return categorias.findById(limpio).orElseThrow(() -> ReglaNegocioException.validacion("DATOS_INVALIDOS",
                "La categoría seleccionada no es válida.", Map.of("category", "La categoría seleccionada no es válida.")));
    }

    private static DescuentoDatos aDatos(DescuentoRequest r) {
        return new DescuentoDatos(r.benefit(), r.conditions(), r.startDate(), r.endDate(), r.code(), r.location(),
                r.website());
    }

    private DescuentoResponse respuesta(Descuento d) {
        return Mapeos.descuento(d, LocalDate.now(reloj), true);
    }
}
