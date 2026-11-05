package com.promou.web;

import com.promou.aplicacion.ConsultaDescuentosService;
import com.promou.aplicacion.dto.DescuentoDtos.DescuentoResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lado estudiante: HU 2.2.1 (directorio) y 2.2.2 (detalle). */
@RestController
@RequestMapping("/api/descuentos")
public class DirectorioController {

    private final ConsultaDescuentosService consulta;

    public DirectorioController(ConsultaDescuentosService consulta) {
        this.consulta = consulta;
    }

    @GetMapping
    public List<DescuentoResponse> directorio() {
        return consulta.directorio();
    }

    @GetMapping("/{id}")
    public DescuentoResponse detalle(@PathVariable UUID id) {
        return consulta.detalle(id);
    }
}
