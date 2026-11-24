package com.promou.web;

import com.promou.aplicacion.ResenaService;
import com.promou.aplicacion.dto.DescuentoDtos.ResenaModeracion;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Moderación de reseñas por el administrador (sección 7.5, deuda técnica 10). */
@RestController
@RequestMapping("/api/admin/resenas")
public class ResenaAdminController {

    private final ResenaService resenas;

    public ResenaAdminController(ResenaService resenas) {
        this.resenas = resenas;
    }

    @GetMapping
    public List<ResenaModeracion> recientes() {
        return resenas.recientes();
    }

    @PostMapping("/{id}/retirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@PathVariable UUID id) {
        resenas.retirar(id);
    }
}
