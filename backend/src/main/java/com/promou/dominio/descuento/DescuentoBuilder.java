package com.promou.dominio.descuento;

import com.promou.comun.Texto;
import com.promou.dominio.usuario.Establecimiento;
import java.time.LocalDate;

/**
 * Patrón Builder: un descuento tiene muchos datos y varios opcionales (sitio web, código),
 * así que se arma paso a paso en lugar de usar un constructor de 8 parámetros.
 * El builder solo construye; la validación es responsabilidad de {@link Descuento} (Information Expert).
 */
public class DescuentoBuilder {

    private Establecimiento establecimiento;
    private Categoria categoria;
    private String beneficio;
    private String condiciones;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String codigo;
    private String ubicacion;
    private String sitioWeb;

    public DescuentoBuilder paraEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
        return this;
    }

    public DescuentoBuilder conBeneficio(String beneficio) {
        this.beneficio = Texto.recortar(beneficio);
        return this;
    }

    public DescuentoBuilder conCondiciones(String condiciones) {
        this.condiciones = Texto.recortar(condiciones);
        return this;
    }

    public DescuentoBuilder conVigencia(LocalDate inicio, LocalDate fin) {
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        return this;
    }

    public DescuentoBuilder conUbicacion(String ubicacion) {
        this.ubicacion = Texto.recortar(ubicacion);
        return this;
    }

    public DescuentoBuilder conCategoria(Categoria categoria) {
        this.categoria = categoria;
        return this;
    }

    /** Si falta el esquema se asume https, igual que normalizeWebsite() en el front. */
    public DescuentoBuilder conSitioWeb(String sitioWeb) {
        String limpio = Texto.nuloSiVacio(sitioWeb);
        this.sitioWeb = (limpio == null || limpio.contains("://")) ? limpio : "https://" + limpio;
        return this;
    }

    public DescuentoBuilder conCodigo(String codigo) {
        this.codigo = Texto.nuloSiVacio(codigo);
        return this;
    }

    /** construir(): Descuento, como en el diagrama de la sección 5.2. */
    public Descuento construir() {
        return new Descuento(establecimiento, categoria, beneficio, condiciones, fechaInicio, fechaFin, codigo,
                ubicacion, sitioWeb);
    }
}
