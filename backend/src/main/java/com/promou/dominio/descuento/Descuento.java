package com.promou.dominio.descuento;

import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.dominio.usuario.Establecimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * GRASP Information Expert: el descuento tiene los datos, así que él valida su vigencia y sus campos
 * obligatorios, decide si está disponible y a qué método de validación corresponde.
 */
@Entity
@Table(name = "descuento")
public class Descuento {

    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9_-]{3,40}$");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 255)
    private String beneficio;

    @Column(nullable = false, length = 2000)
    private String condiciones;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(length = 40)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String ubicacion;

    @Column(name = "sitio_web", length = 500)
    private String sitioWeb;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Descuento() {
    }

    Descuento(Establecimiento establecimiento, Categoria categoria, String beneficio, String condiciones,
              LocalDate fechaInicio, LocalDate fechaFin, String codigo, String ubicacion, String sitioWeb) {
        this.establecimiento = establecimiento;
        this.categoria = categoria;
        this.beneficio = beneficio;
        this.condiciones = condiciones;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.codigo = codigo;
        this.ubicacion = ubicacion;
        this.sitioWeb = sitioWeb;
        this.activo = true;
    }

    /** Marca de tiempo de creación/edición; la fija el caso de uso para que el dominio no dependa del reloj. */
    public void registrarFechas(Instant ahora) {
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        fechaActualizacion = ahora;
    }

    /** validarDatos() + validarVigencia(): lanza un error con un mensaje por campo (HU 2.1.1 esc. 2 y 3). */
    public void validar() {
        Map<String, String> errores = new LinkedHashMap<>();
        if (Texto.vacio(beneficio)) {
            errores.put("benefit", "El beneficio es obligatorio.");
        } else if (beneficio.length() > 255) {
            errores.put("benefit", "El beneficio no puede superar 255 caracteres.");
        }
        if (Texto.vacio(condiciones)) {
            errores.put("conditions", "Las condiciones son obligatorias.");
        } else if (condiciones.length() > 2000) {
            errores.put("conditions", "Las condiciones no pueden superar 2000 caracteres.");
        }
        if (fechaInicio == null) {
            errores.put("startDate", "La fecha de inicio es obligatoria.");
        }
        if (fechaFin == null) {
            errores.put("endDate", "La fecha de finalización es obligatoria.");
        }
        if (!validarVigencia() && fechaInicio != null && fechaFin != null) {
            errores.put("endDate",
                    "La vigencia no es válida: la fecha de finalización no puede ser anterior a la fecha de inicio.");
        }
        if (Texto.vacio(ubicacion)) {
            errores.put("location", "La ubicación es obligatoria.");
        } else if (ubicacion.length() > 255) {
            errores.put("location", "La ubicación no puede superar 255 caracteres.");
        }
        if (categoria == null) {
            errores.put("category", "La categoría es obligatoria.");
        }
        if (codigo != null && !CODIGO.matcher(codigo).matches()) {
            errores.put("code", "El código solo puede tener letras, números, guion y guion bajo (3 a 40 caracteres).");
        }
        if (sitioWeb != null && !esUrlValida(sitioWeb)) {
            errores.put("website", "Ingresa una dirección web válida (http o https).");
        }
        if (!errores.isEmpty()) {
            throw ReglaNegocioException.validacion("DATOS_INVALIDOS",
                    "Revisa la información del descuento.", errores);
        }
    }

    /**
     * HU 2.1.2: aplica los cambios solo si el descuento editado es válido (esc. 2 y 3); si no, conserva los datos
     * anteriores. El candidato se arma con el mismo Builder de la publicación y se valida igual.
     */
    public void editar(DescuentoDatos datos, Categoria nuevaCategoria, Instant ahora) {
        Descuento candidato = new DescuentoBuilder()
                .paraEstablecimiento(establecimiento)
                .conBeneficio(datos.benefit())
                .conCondiciones(datos.conditions())
                .conVigencia(datos.startDate(), datos.endDate())
                .conUbicacion(datos.location())
                .conCategoria(nuevaCategoria)
                .conSitioWeb(datos.website())
                .conCodigo(datos.code())
                .construir();
        candidato.validar();
        this.beneficio = candidato.beneficio;
        this.condiciones = candidato.condiciones;
        this.fechaInicio = candidato.fechaInicio;
        this.fechaFin = candidato.fechaFin;
        this.ubicacion = candidato.ubicacion;
        this.categoria = candidato.categoria;
        this.sitioWeb = candidato.sitioWeb;
        this.codigo = candidato.codigo;
        registrarFechas(ahora);
    }

    /** HU 2.1.3: deja de estar disponible para los estudiantes. Esc. 4: no se desactiva dos veces. */
    public void desactivar(Instant ahora) {
        if (!activo) {
            throw ReglaNegocioException.conflicto("DESCUENTO_YA_DESACTIVADO", "El descuento ya se encuentra desactivado.");
        }
        activo = false;
        registrarFechas(ahora);
    }

    /** HU 2.2.5: solo hay código para copiar si el descuento lo tiene y sigue disponible (esc. 2 y 4). */
    public Optional<String> codigoCopiable(LocalDate hoy) {
        return codigo != null && estaDisponible(hoy) ? Optional.of(codigo) : Optional.empty();
    }

    /** true si no hay fechas incompletas y fin no es anterior a inicio. */
    public boolean validarVigencia() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }

    private static boolean esUrlValida(String url) {
        if (url.length() > 500) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String esquema = uri.getScheme();
            return ("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                    && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Disponible = activo y hoy dentro de la vigencia. */
    public boolean estaDisponible(LocalDate hoy) {
        return activo && !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public UUID getId() {
        return id;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getBeneficio() {
        return beneficio;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public boolean isActivo() {
        return activo;
    }
}
