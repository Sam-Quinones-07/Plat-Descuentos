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
import org.junit.jupiter.api.Test;

class DescuentoTest {

    private static final Instant AHORA = Instant.parse("2026-10-26T12:00:00Z");
    private static final Categoria RESTAURANTES = new Categoria("restaurants", "Restaurantes", 1);

    private final Establecimiento burger = Establecimiento.solicitarAfiliacion("contacto@burgerlocalmed.com", "hash",
            new Establecimiento.Datos("Burger Local", "Inversiones Burger Local S.A.S.", "9012345678", "(604) 321 4567",
                    "Circular 1ra # 70-20", "Carlos Restrepo", "1036987654", "carlos@burgerlocalmed.com"), AHORA);

    private DescuentoDatos datos(String beneficio, LocalDate inicio, LocalDate fin, String codigo) {
        return new DescuentoDatos(beneficio, "Válido de lunes a viernes.", inicio, fin, codigo, "Laureles, Medellín",
                "burgerlocalmed.com");
    }

    @Test
    void creaDescuentoValidoYEstaDisponibleSoloDentroDeLaVigencia() {
        Descuento d = burger.crearDescuento(
                datos("20% en hamburguesas", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), "U-BURGER20"),
                RESTAURANTES);
        d.validar();
        assertTrue(d.estaDisponible(LocalDate.of(2026, 10, 1)));
        assertTrue(d.estaDisponible(LocalDate.of(2026, 10, 15)));
        assertFalse(d.estaDisponible(LocalDate.of(2026, 9, 30)));
        assertFalse(d.estaDisponible(LocalDate.of(2026, 10, 16)));
    }

    @Test
    void informaCadaCampoObligatorioFaltante() {
        Descuento d = burger.crearDescuento(new DescuentoDatos(" ", "", null, null, null, "", null), null);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, d::validar);
        assertEquals("DATOS_INVALIDOS", ex.getCodigo());
        for (String campo : new String[] {"benefit", "conditions", "startDate", "endDate", "location", "category"}) {
            assertTrue(ex.getCampos().containsKey(campo), "falta el error de " + campo);
        }
    }

    @Test
    void rechazaFinAnteriorAInicioPeroPermiteElMismoDia() {
        Descuento malo = burger.crearDescuento(
                datos("x", LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1), null), RESTAURANTES);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, malo::validar);
        assertTrue(ex.getCampos().containsKey("endDate"));

        Descuento mismoDia = burger.crearDescuento(
                datos("x", LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 15), null), RESTAURANTES);
        mismoDia.validar();
    }

    @Test
    void normalizaYValidaElSitioWebYElCodigo() {
        Descuento d = burger.crearDescuento(datos("x", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), null), RESTAURANTES);
        assertEquals("https://burgerlocalmed.com", d.getSitioWeb());

        Descuento malo = burger.crearDescuento(new DescuentoDatos("x", "y", LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2), "a b!", "Medellín", "http://"), RESTAURANTES);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, malo::validar);
        assertTrue(ex.getCampos().containsKey("website"));
        assertTrue(ex.getCampos().containsKey("code"));
    }

}
