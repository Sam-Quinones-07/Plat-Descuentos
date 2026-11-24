package com.promou.web;

import com.promou.aplicacion.ConsultaDescuentosService;
import com.promou.aplicacion.GuardadoService;
import com.promou.aplicacion.ResenaService;
import com.promou.aplicacion.dto.AuthDtos.MensajeResponse;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import com.promou.aplicacion.dto.DescuentoDtos.PaginaResponse;
import com.promou.aplicacion.dto.DescuentoDtos.ResenaRequest;
import com.promou.aplicacion.dto.DescuentoDtos.ResenaResponse;
import com.promou.seguridad.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Lado estudiante: HU 2.2.1 a 2.2.5, 3.1.1 y 3.1.2. */
@RestController
public class DirectorioController {

    private final ConsultaDescuentosService consulta;
    private final GuardadoService guardados;
    private final ResenaService resenas;

    public DirectorioController(ConsultaDescuentosService consulta, GuardadoService guardados, ResenaService resenas) {
        this.consulta = consulta;
        this.guardados = guardados;
        this.resenas = resenas;
    }

    /** ?categoria=restaurants (HU 2.2.3) &pagina=0&tamano=12 (deuda técnica 8). */
    @GetMapping("/api/descuentos")
    public PaginaResponse<DescuentoResponse> directorio(@AuthenticationPrincipal UsuarioAutenticado u,
                                                        @RequestParam(required = false) String categoria,
                                                        @RequestParam(defaultValue = "0") int pagina,
                                                        @RequestParam(defaultValue = "12") int tamano) {
        return consulta.directorio(categoria, pagina, tamano, u.usuarioId());
    }

    @GetMapping("/api/descuentos/{id}")
    public DescuentoResponse detalle(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return consulta.detalle(id, u.usuarioId());
    }

    @GetMapping("/api/descuentos/{id}/resenas")
    public List<ResenaResponse> resenas(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return resenas.listar(id, u.usuarioId());
    }

    @PostMapping("/api/descuentos/{id}/resenas")
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaResponse publicarResena(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id,
                                         @Valid @RequestBody ResenaRequest r) {
        return resenas.publicar(u.usuarioId(), id, r.text());
    }

    @GetMapping("/api/estudiantes/me/guardados")
    public List<DescuentoResponse> misGuardados(@AuthenticationPrincipal UsuarioAutenticado u) {
        return guardados.listar(u.usuarioId());
    }

    @PostMapping("/api/estudiantes/me/guardados/{descuentoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse guardar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID descuentoId) {
        return guardados.guardar(u.usuarioId(), descuentoId);
    }

    @DeleteMapping("/api/estudiantes/me/guardados/{descuentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID descuentoId) {
        guardados.quitar(u.usuarioId(), descuentoId);
    }
}
