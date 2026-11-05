package com.promou.dominio.usuario;

import com.promou.comun.ReglaNegocioException;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import com.promou.dominio.afiliacion.SolicitudAfiliacion;
import com.promou.dominio.descuento.Categoria;
import com.promou.dominio.descuento.Descuento;
import com.promou.dominio.descuento.DescuentoBuilder;
import com.promou.dominio.descuento.DescuentoDatos;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * El "correo" heredado de Usuario es el correo del establecimiento (con el que inicia sesión, HU 1.2.4);
 * la contraseña es la que definió el representante al solicitar la afiliación.
 */
@Entity
@Table(name = "establecimiento")
public class Establecimiento extends Usuario {

    /** Datos del formulario de afiliación (sin credenciales). */
    public record Datos(String nombreComercial, String razonSocial, String nit, String telefono, String direccion,
                        String representanteNombre, String representanteDocumento, String representanteCorreo) {
    }

    @Column(nullable = false, length = 20)
    private String nit;

    @Column(name = "nit_normalizado", nullable = false, length = 20)
    private String nitNormalizado;

    @Column(name = "nombre_comercial", nullable = false, length = 120)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 160)
    private String razonSocial;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(name = "representante_nombre", nullable = false, length = 120)
    private String representanteNombre;

    @Column(name = "representante_documento", nullable = false, length = 24)
    private String representanteDocumento;

    @Column(name = "representante_correo", nullable = false, length = 254)
    private String representanteCorreo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAfiliacion solicitud;

    @OneToMany(mappedBy = "establecimiento")
    private List<Descuento> descuentos = new ArrayList<>();

    protected Establecimiento() {
    }

    private Establecimiento(String correo, String hash, Datos d, Instant ahora) {
        super(correo, hash, ahora);
        this.nit = d.nit();
        this.nitNormalizado = normalizarNit(d.nit());
        this.nombreComercial = d.nombreComercial();
        this.razonSocial = d.razonSocial();
        this.telefono = d.telefono();
        this.direccion = d.direccion();
        this.representanteNombre = d.representanteNombre();
        this.representanteDocumento = d.representanteDocumento();
        this.representanteCorreo = d.representanteCorreo();
        this.solicitud = SolicitudAfiliacion.pendiente(ahora);
    }

    /** solicitarAfiliacion(): crea el establecimiento junto con su solicitud en estado PENDIENTE. */
    public static Establecimiento solicitarAfiliacion(String correo, String hash, Datos datos, Instant ahora) {
        return new Establecimiento(correo, hash, datos, ahora);
    }

    public static String normalizarNit(String nit) {
        return nit.replace("-", "").trim();
    }

    /**
     * GRASP Creator: el establecimiento contiene sus descuentos, por eso es quien los crea.
     * Usa el Builder (sección 5.2 del documento: Establecimiento --usa--> DescuentoBuilder --crea--> Descuento).
     */
    public Descuento crearDescuento(DescuentoDatos datos, Categoria categoria) {
        return new DescuentoBuilder()
                .paraEstablecimiento(this)
                .conBeneficio(datos.benefit())
                .conCondiciones(datos.conditions())
                .conVigencia(datos.startDate(), datos.endDate())
                .conUbicacion(datos.location())
                .conCategoria(categoria)
                .conSitioWeb(datos.website())
                .conCodigo(datos.code())
                .construir();
    }

    public void agregarDescuento(Descuento descuento) {
        descuentos.add(descuento);
    }

    /** Solo un establecimiento con correo confirmado y afiliación aprobada puede gestionar descuentos. */
    public void exigirOperativo() {
        if (!isCorreoConfirmado() || solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            throw ReglaNegocioException.prohibido("ESTABLECIMIENTO_NO_AFILIADO",
                    "Tu establecimiento aún no está afiliado a la plataforma.");
        }
    }

    @Override
    public void verificarPuedeIniciarSesion() {
        super.verificarPuedeIniciarSesion();
        solicitud.getEstado().verificarAccesoLogin();
    }

    @Override
    public Rol getRol() {
        return Rol.ESTABLECIMIENTO;
    }

    @Override
    public String getNombreVisible() {
        return nombreComercial;
    }

    public EstadoSolicitud getEstadoAfiliacion() {
        return solicitud.getEstado();
    }

    public SolicitudAfiliacion getSolicitud() {
        return solicitud;
    }

    public String getNit() {
        return nit;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getRepresentanteNombre() {
        return representanteNombre;
    }

    public String getRepresentanteDocumento() {
        return representanteDocumento;
    }

    public String getRepresentanteCorreo() {
        return representanteCorreo;
    }
}
