package com.promou.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.promou.aplicacion.dto.AuthDtos.CambioContrasenaRequest;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoRequest;
import com.promou.aplicacion.dto.DescuentoDtos.ValidacionQrResponse;
import com.promou.aplicacion.eventos.CorreoPendiente;
import com.promou.comun.LimitadorPeticiones;
import com.promou.comun.ReglaNegocioException;
import com.promou.config.PromoUProperties;
import com.promou.dominio.credencial.CredencialDigital;
import com.promou.dominio.credencial.CredencialRepository;
import com.promou.dominio.cuenta.TokenVerificacion;
import com.promou.dominio.cuenta.TokenVerificacionRepository;
import com.promou.dominio.descuento.CategoriaRepository;
import com.promou.dominio.descuento.DescuentoRepository;
import com.promou.dominio.guardado.DescuentoGuardado;
import com.promou.dominio.guardado.GuardadoRepository;
import com.promou.dominio.resena.ResenaRepository;
import com.promou.dominio.usuario.Establecimiento;
import com.promou.dominio.usuario.EstablecimientoRepository;
import com.promou.dominio.usuario.Estudiante;
import com.promou.dominio.usuario.EstudianteRepository;
import com.promou.dominio.usuario.UsuarioRepository;
import com.promou.seguridad.ControlIntentosService;
import com.promou.seguridad.SesionService;
import com.promou.seguridad.UsuarioAutenticado;
import com.promou.dominio.usuario.Rol;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Pruebas de los casos de uso del Release 2 (sección 7.3), con repositorios simulados. */
class ServiciosRelease2Test {

    private static final Instant AHORA = Instant.parse("2026-11-20T15:00:00Z");
    private final Clock reloj = Clock.fixed(AHORA, ZoneId.of("America/Bogota"));
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final PromoUProperties props = new Binder(new MapConfigurationPropertySource(
            java.util.Map.of("promou.front.url", "https://promou.test")))
            .bindOrCreate("promou", Bindable.of(PromoUProperties.class));

    private Estudiante estudiante(boolean verificado) {
        Estudiante e = new Estudiante("laura@udea.edu.co", encoder.encode("Universitario@26"), "Laura", "Gómez",
                "UdeA", AHORA);
        if (verificado) {
            e.confirmarCorreo();
        }
        return e;
    }

    private Establecimiento establecimiento() {
        return Establecimiento.solicitarAfiliacion("contacto@burger.co", "hash",
                new Establecimiento.Datos("Burger Local", "Burger SAS", "900123456-7", "6045551234", "Cra 70",
                        "Carlos", "1000000001", "carlos@burger.co"), AHORA);
    }

    // ---------- HU 1.2.2 esc. 5 ----------

    @Test
    void reenviarGeneraUnEnlaceNuevoYAnulaElAnterior() {
        TokenVerificacionRepository tokens = mock(TokenVerificacionRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
        EnlacesCorreo enlaces = new EnlacesCorreo(tokens, new LimitadorPeticiones(3, Duration.ofMinutes(15)), eventos,
                props, reloj);
        when(usuarios.findByCorreo("contacto@burger.co")).thenReturn(Optional.of(establecimiento()));
        var servicio = new ConfirmacionCorreoService(tokens, usuarios, enlaces, eventos, props, reloj);

        servicio.reenviar("Contacto@Burger.co");

        verify(tokens, times(1)).anularAnteriores(any(), any());
        verify(tokens, times(1)).save(any(TokenVerificacion.class));
        verify(eventos, times(1)).publishEvent(any(CorreoPendiente.class));
    }

    // ---------- HU 1.3.1 ----------

    @Test
    void cambioDeContrasenaCierraLasDemasSesiones() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        SesionService sesiones = mock(SesionService.class);
        Estudiante e = estudiante(true);
        UUID id = UUID.randomUUID();
        UUID sesion = UUID.randomUUID();
        when(usuarios.findById(id)).thenReturn(Optional.of(e));

        new CuentaService(usuarios, encoder, sesiones).cambiarContrasena(new UsuarioAutenticado(id, sesion,
                Rol.ESTUDIANTE), new CambioContrasenaRequest("Universitario@26", "Nueva@2026", "Nueva@2026"));

        assertTrue(encoder.matches("Nueva@2026", e.getContrasenaHash()));
        verify(sesiones, times(1)).cerrarOtras(any(), any());
    }

    // ---------- HU 1.3.2 ----------

    private RecuperacionContrasenaService recuperacion(UsuarioRepository usuarios, TokenVerificacionRepository tokens,
                                                       ApplicationEventPublisher eventos, SesionService sesiones) {
        EnlacesCorreo enlaces = new EnlacesCorreo(tokens, new LimitadorPeticiones(3, Duration.ofMinutes(15)), eventos,
                props, reloj);
        return new RecuperacionContrasenaService(usuarios, tokens, enlaces, sesiones,
                mock(ControlIntentosService.class), encoder, props, reloj);
    }

    @Test
    void correoNoRegistradoRecibeLaMismaRespuestaSinEnviarCorreo() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
        when(usuarios.findByCorreo(any())).thenReturn(Optional.empty());
        var servicio = recuperacion(usuarios, mock(TokenVerificacionRepository.class), eventos,
                mock(SesionService.class));

        assertEquals(RecuperacionContrasenaService.MENSAJE_SOLICITUD,
                servicio.solicitar("noexiste@udea.edu.co").message());
        verify(eventos, never()).publishEvent(any());
    }

    @Test
    void restablecerReemplazaLaContrasenaYCierraTodasLasSesiones() {
        TokenVerificacionRepository tokens = mock(TokenVerificacionRepository.class);
        SesionService sesiones = mock(SesionService.class);
        Estudiante e = estudiante(true);
        TokenVerificacion t = TokenVerificacion.emitir(e, TokenVerificacion.Tipo.RESTABLECIMIENTO, "h",
                AHORA.minusSeconds(60), Duration.ofHours(1));
        when(tokens.buscar(any(), any())).thenReturn(Optional.of(t));
        var servicio = recuperacion(mock(UsuarioRepository.class), tokens, mock(ApplicationEventPublisher.class),
                sesiones);

        servicio.restablecer("token", "Otra@2026x", "Otra@2026x");

        assertTrue(encoder.matches("Otra@2026x", e.getContrasenaHash()));
        assertFalse(encoder.matches("Universitario@26", e.getContrasenaHash()));
        verify(sesiones, times(1)).cerrarTodas(any());
        assertEquals("ENLACE_INVALIDO", assertThrows(ReglaNegocioException.class,
                () -> servicio.restablecer("token", "Otra@2026y", "Otra@2026y")).getCodigo());
    }

    // ---------- HU 1.4.2 ----------

    private ValidacionQrResponse validar(Optional<CredencialDigital> credencial) {
        CredencialRepository credenciales = mock(CredencialRepository.class);
        EstablecimientoRepository establecimientos = mock(EstablecimientoRepository.class);
        Establecimiento comercio = establecimiento();
        comercio.confirmarCorreo();
        comercio.getSolicitud().aprobar(null, null, AHORA);
        when(establecimientos.findById(any())).thenReturn(Optional.of(comercio));
        when(credenciales.buscarPorCodigo(any())).thenReturn(credencial);
        return new CredencialService(credenciales, mock(EstudianteRepository.class), establecimientos, props, reloj)
                .validarQr(UUID.randomUUID(), "u-abcdefghjk");
    }

    @Test
    void qrVigenteEsValidoYNoExponeDatosSensibles() {
        CredencialDigital c = estudiante(true).generarCredencial("U-ABCDEFGHJK", AHORA, Duration.ofDays(365));
        ValidacionQrResponse r = validar(Optional.of(c));
        assertEquals("VALIDA", r.result());
        assertEquals("Laura G.", r.studentName());
        assertEquals("UdeA", r.university());
    }

    @Test
    void qrVencidoNoEstaVigente() {
        CredencialDigital c = estudiante(true).generarCredencial("U-ABCDEFGHJK", AHORA.minus(Duration.ofDays(400)),
                Duration.ofDays(365));
        ValidacionQrResponse r = validar(Optional.of(c));
        assertEquals("NO_VIGENTE", r.result());
        assertNull(r.studentName());
    }

    @Test
    void qrNoRegistradoNoSeReconoce() {
        assertEquals("NO_RECONOCIDA", validar(Optional.empty()).result());
    }

    // ---------- HU 2.1.2: control de acceso ----------

    @Test
    void unEstablecimientoNoPuedeEditarDescuentosAjenos() {
        EstablecimientoRepository establecimientos = mock(EstablecimientoRepository.class);
        DescuentoRepository descuentos = mock(DescuentoRepository.class);
        Establecimiento comercio = establecimiento();
        comercio.confirmarCorreo();
        comercio.getSolicitud().aprobar(null, null, AHORA);
        when(establecimientos.findById(any())).thenReturn(Optional.of(comercio));
        when(descuentos.buscarDelEstablecimiento(any(), any())).thenReturn(Optional.empty());
        var servicio = new GestionDescuentosService(establecimientos, descuentos, mock(CategoriaRepository.class),
                reloj);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.editar(UUID.randomUUID(), UUID.randomUUID(),
                        new DescuentoRequest("x", "y", null, null, null, "z", null, "restaurants")));
        assertEquals("DESCUENTO_NO_ENCONTRADO", ex.getCodigo());
    }

    // ---------- HU 2.2.4 esc. 4 ----------

    @Test
    void guardarUnDescuentoYaGuardadoNoDuplica() {
        GuardadoRepository guardados = mock(GuardadoRepository.class);
        DescuentoRepository descuentos = mock(DescuentoRepository.class);
        var d = establecimiento().crearDescuento(new com.promou.dominio.descuento.DescuentoDatos("20%", "L-J",
                java.time.LocalDate.of(2026, 11, 1), java.time.LocalDate.of(2026, 12, 31), null, "Cra 70", null),
                new com.promou.dominio.descuento.Categoria("restaurants", "Restaurantes", 1));
        when(descuentos.findById(any())).thenReturn(Optional.of(d));
        when(guardados.existePor(any(), any())).thenReturn(true);
        var servicio = new GuardadoService(guardados, descuentos, mock(EstudianteRepository.class), reloj);

        assertEquals("DESCUENTO_YA_GUARDADO", assertThrows(ReglaNegocioException.class,
                () -> servicio.guardar(UUID.randomUUID(), UUID.randomUUID())).getCodigo());
        verify(guardados, never()).save(any(DescuentoGuardado.class));
    }

    // ---------- HU 3.1.2: una reseña por estudiante y descuento ----------

    @Test
    void unEstudianteNoPublicaDosResenasDelMismoDescuento() {
        ResenaRepository resenas = mock(ResenaRepository.class);
        DescuentoRepository descuentos = mock(DescuentoRepository.class);
        EstudianteRepository estudiantes = mock(EstudianteRepository.class);
        var d = establecimiento().crearDescuento(new com.promou.dominio.descuento.DescuentoDatos("20%", "L-J",
                java.time.LocalDate.of(2026, 11, 1), java.time.LocalDate.of(2026, 12, 31), null, "Cra 70", null),
                new com.promou.dominio.descuento.Categoria("restaurants", "Restaurantes", 1));
        when(descuentos.findById(any())).thenReturn(Optional.of(d));
        when(estudiantes.findById(any())).thenReturn(Optional.of(estudiante(true)));
        when(resenas.existePor(any(), any())).thenReturn(true);
        var servicio = new ResenaService(resenas, descuentos, estudiantes, reloj);

        assertEquals("RESENA_YA_PUBLICADA", assertThrows(ReglaNegocioException.class,
                () -> servicio.publicar(UUID.randomUUID(), UUID.randomUUID(), "Me aplicaron el descuento")).getCodigo());
    }

    // ---------- HU 2.2.3: filtrar y quitar el filtro por categoría ----------

    @Test
    void filtrarPorCategoriaYQuitarElFiltro() {
        DescuentoRepository descuentos = mock(DescuentoRepository.class);
        var restaurantes = new com.promou.dominio.descuento.Categoria("restaurants", "Restaurantes", 1);
        var tecnologia = new com.promou.dominio.descuento.Categoria("technology", "Tecnología", 3);
        Establecimiento comercio = establecimiento();
        java.util.List<com.promou.dominio.descuento.Descuento> todos = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            todos.add(comercio.crearDescuento(new com.promou.dominio.descuento.DescuentoDatos(i + "0%", "L-J",
                    java.time.LocalDate.of(2026, 11, 1), java.time.LocalDate.of(2026, 12, 31), null, "Cra 70", null),
                    i < 2 ? restaurantes : tecnologia));
        }
        when(descuentos.disponibles(any(), eq("restaurants"), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(todos.subList(0, 2)));
        when(descuentos.disponibles(any(), isNull(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(todos));
        var servicio = new ConsultaDescuentosService(descuentos, mock(GuardadoRepository.class), reloj);

        var filtrados = servicio.directorio("restaurants", 0, 12, UUID.randomUUID());
        assertEquals(2, filtrados.items().size());
        assertTrue(filtrados.items().stream().allMatch(d -> d.category().equals("restaurants")));
        assertEquals(5, servicio.directorio(null, 0, 12, UUID.randomUUID()).items().size());
        assertEquals(5, servicio.directorio("  ", 0, 12, UUID.randomUUID()).items().size());
    }
}
