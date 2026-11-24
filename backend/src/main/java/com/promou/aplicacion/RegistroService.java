package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstablecimientoRequest;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstudianteRequest;
import com.promou.comun.DominioInstitucional;
import com.promou.comun.PoliticaContrasena;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.config.PromoUProperties;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.EstablecimientoRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.UsuarioRepository;
import java.time.Clock;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 1.1.1 (registrar estudiante) y 1.2.1 (registrar establecimiento).
 * La cuenta queda sin confirmar hasta que el usuario abre el enlace del correo (HU 1.1.2, 1.2.2). El registro guarda
 * la aceptación de los términos y la política de datos (sección 11.1).
 */
@Service
public class RegistroService {

    private final UsuarioRepository usuarios;
    private final EstablecimientoRepository establecimientos;
    private final DominioInstitucional dominios;
    private final PasswordEncoder encoder;
    private final ConfirmacionCorreoService confirmacion;
    private final PromoUProperties props;
    private final Clock reloj;

    public RegistroService(UsuarioRepository usuarios, EstablecimientoRepository establecimientos,
                           DominioInstitucional dominios, PasswordEncoder encoder,
                           ConfirmacionCorreoService confirmacion, PromoUProperties props, Clock reloj) {
        this.usuarios = usuarios;
        this.establecimientos = establecimientos;
        this.dominios = dominios;
        this.encoder = encoder;
        this.confirmacion = confirmacion;
        this.props = props;
        this.reloj = reloj;
    }

    @Transactional
    public MensajeResponse registrarEstudiante(RegistroEstudianteRequest r) {
        String correo = Texto.normalizarCorreo(r.email());
        if (!dominios.esInstitucional(correo)) { // esc. 4
            throw ReglaNegocioException.validacion("CORREO_NO_INSTITUCIONAL", "Debes usar tu correo institucional.",
                    Map.of("email", "Debes usar tu correo institucional."));
        }
        validarContrasena(r.password(), r.confirmation()); // esc. 6 y 7
        if (usuarios.existsByCorreo(correo)) { // esc. 2: la HU exige un mensaje explícito
            throw ReglaNegocioException.conflicto("CORREO_EN_USO", "Este correo electrónico ya está en uso.");
        }
        Estudiante estudiante = new Estudiante(correo, encoder.encode(r.password()), r.firstName().trim(),
                r.lastName().trim(), r.university().trim(), reloj.instant());
        estudiante.aceptarTerminos(props.front().versionTerminos(), reloj.instant());
        estudiante = usuarios.save(estudiante);
        confirmacion.enviarEnlace(estudiante); // HU 1.1.2 esc. 1; al confirmar se emite la identificación (HU 1.4.1)
        return new MensajeResponse("Registro exitoso. Revisa tu correo y abre el enlace para confirmar tu cuenta.");
    }

    /** Los conflictos usan mensajes genéricos (HU 1.2.1 esc. 3 y 5): no se revela de quién es el NIT o el correo. */
    @Transactional
    public MensajeResponse registrarEstablecimiento(RegistroEstablecimientoRequest r) {
        validarContrasena(r.password(), r.confirmation());
        String correo = Texto.normalizarCorreo(r.businessEmail());
        String nitNormalizado = Establecimiento.normalizarNit(r.nit().trim());

        var existente = establecimientos.findByNitNormalizado(nitNormalizado);
        if (existente.isPresent() && existente.get().getEstadoAfiliacion() == EstadoSolicitud.PENDIENTE) {
            throw ReglaNegocioException.conflicto("SOLICITUD_NO_PROCESABLE", "No es posible procesar la solicitud.");
        }
        if (existente.isPresent() || usuarios.existsByCorreo(correo)) {
            throw ReglaNegocioException.conflicto("REGISTRO_NO_DISPONIBLE",
                    "No es posible completar el registro con la información ingresada.");
        }
        Establecimiento.Datos datos = new Establecimiento.Datos(r.tradeName().trim(), r.legalName().trim(),
                r.nit().trim(), r.phone().trim(), r.address().trim(), r.representativeName().trim(),
                r.document().trim(), Texto.normalizarCorreo(r.email()));
        Establecimiento e = Establecimiento.solicitarAfiliacion(correo, encoder.encode(r.password()), datos,
                reloj.instant());
        e.aceptarTerminos(props.front().versionTerminos(), reloj.instant());
        usuarios.save(e);
        confirmacion.enviarEnlace(e); // HU 1.2.2 esc. 1
        return new MensajeResponse("Tu solicitud de afiliación fue registrada exitosamente. "
                + "Te enviamos un correo para confirmar el registro.");
    }

    private static void validarContrasena(String contrasena, String confirmacion) {
        PoliticaContrasena.exigir(contrasena, "password");
        if (confirmacion != null && !confirmacion.equals(contrasena)) {
            throw ReglaNegocioException.validacion("DATOS_INVALIDOS", "Las contraseñas no coinciden.",
                    Map.of("confirmation", "Las contraseñas no coinciden."));
        }
    }
}
