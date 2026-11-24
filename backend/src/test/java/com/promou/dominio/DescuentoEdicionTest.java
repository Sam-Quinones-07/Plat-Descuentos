package com.promou.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.descuento.Categoria;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoDatos;
import com.promou.dominio.usuario.Establecimiento;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** HU 2.1.2 (editar), 2.1.3 (desactivar) y 2.2.5 (código copiable). */
class DescuentoEdicionTest {

    private static final Instant AHORA = Instant.parse("2026-11-20T10:00:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 20);
    private static final Categoria RESTAURANTES = new Categoria("restaurants", "Restaurantes", 1);

    private Descuento descuento(String codigo) {
        Establecimiento e = Establecimiento.solicitarAfiliacion("contacto@burger.co", "hash",
                new Establecimiento.Datos("Burger Local", "Burger SAS", "900123456-7", "6045551234", "Cra 70",
                        "Carlos", "1000000001", "carlos@burger.co"), AHORA);
        Descuento d = e.crearDescuento(new DescuentoDatos("20% en hamburguesas", "L-J", HOY.minusDays(5),
                HOY.plusDays(20), codigo, "Cra 70", null), RESTAURANTES);
        d.validar();
        d.registrarFechas(AHORA);
        return d;
    }

    @Test
    void vigenciaInvalidaAlEditarConservaLosDatosOriginales() {
        Descuento d = descuento("BURGER20");
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> d.editar(
                new DescuentoDatos("30%", "L-J", HOY.plusDays(10), HOY, null, "Cra 70", null), RESTAURANTES, AHORA));
        assertEquals("DATOS_INVALIDOS", ex.getCodigo());
        assertTrue(ex.getCampos().containsKey("endDate"));
        assertEquals("20% en hamburguesas", d.getBeneficio());
        assertEquals(HOY.plusDays(20), d.getFechaFin());
    }

    @Test
    void edicionValidaActualizaElDescuento() {
        Descuento d = descuento("BURGER20");
        d.editar(new DescuentoDatos("25% en hamburguesas", "L-V", HOY, HOY.plusDays(30), "BURGER25", "Cra 70",
                "burger.co"), RESTAURANTES, AHORA);
        assertEquals("25% en hamburguesas", d.getBeneficio());
        assertEquals("BURGER25", d.getCodigo());
        assertEquals("https://burger.co", d.getSitioWeb());
    }

    @Test
    void desactivarSoloUnaVez() {
        Descuento d = descuento(null);
        d.desactivar(AHORA);
        assertFalse(d.estaDisponible(HOY));
        assertEquals("DESCUENTO_YA_DESACTIVADO",
                assertThrows(ReglaNegocioException.class, () -> d.desactivar(AHORA)).getCodigo());
    }

    @Test
    void codigoCopiableSoloSiExisteYEstaVigente() {
        assertEquals(Optional.of("BURGER20"), descuento("BURGER20").codigoCopiable(HOY));
        assertEquals(Optional.empty(), descuento(null).codigoCopiable(HOY));
        assertEquals(Optional.empty(), descuento("BURGER20").codigoCopiable(HOY.plusDays(21)));
    }
}
