package com.promou.aplicacion;

import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstablecimientoRequest;
import com.promou.aplicacion.dto.AuthDtos.RegistroEstudianteRequest;
import com.promou.aplicacion.eventos.EstudianteVerificado;
import com.promou.comun.DominioInstitucional;
import com.promou.comun.PoliticaContrasena;
import com.promou.comun.ReglaNegocioException;
import com.promou.comun.Texto;
import com.promou.dominio.afiliacion.EstadoSolicitud;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.EstablecimientoRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.UsuarioRepository;
import java.time.Clock;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 1.1.1 (registrar estudiante) y 1.2.1 (registrar establecimiento).
 * Release 1: no hay confirmación por correo (llega en Release 2). La condición de estudiante se verifica con el
 * dominio institucional del correo, y la cuenta queda lista para usar; el establecimiento queda pendiente de revisión.
 */
@Service
public class RegistroService {

    private final UsuarioRepository usuarios;
    private final EstablecimientoRepository establecimientos;
    private final DominioInstitucional dominios;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher eventos;
    private final Clock reloj;

    public RegistroService(UsuarioRepository usuarios, EstablecimientoRepository establecimientos,
                           DominioInstitucional dominios, PasswordEncoder encoder,
                           ApplicationEventPublisher eventos, Clock reloj) {
        this.usuarios = usuarios;
        this.establecimientos = establecimientos;
        this.dominios = dominios;
        this.encoder = encoder;
        this.eventos = eventos;
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
        estudiante.confirmarCorreo(); // dominio institucional validado => estudiante verificado
        estudiante = usuarios.save(estudiante);
        // Observer: al verificarse el estudiante, CredencialService emite su identificación digital (HU 1.4.1 esc. 1)
        eventos.publishEvent(new EstudianteVerificado(estudiante.getId()));
        return new MensajeResponse("Registro exitoso. Ya puedes iniciar sesión.");
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
        e.confirmarCorreo(); // Release 2 agrega la confirmación por enlace
        usuarios.save(e);
        return new MensajeResponse("Tu solicitud de afiliación fue registrada exitosamente.");
    }

    private static void validarContrasena(String contrasena, String confirmacion) {
        PoliticaContrasena.exigir(contrasena, "password");
        if (confirmacion != null && !confirmacion.equals(contrasena)) {
            throw ReglaNegocioException.validacion("DATOS_INVALIDOS", "Las contraseñas no coinciden.",
                    Map.of("confirmation", "Las contraseñas no coinciden."));
        }
    }
}
