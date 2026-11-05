package com.promou.comun;

import java.util.List;
import java.util.Locale;

/** Verifica que un correo pertenezca a un dominio institucional permitido (HU 1.1.1 esc. 4). */
public class DominioInstitucional {

    private final List<String> dominios;

    public DominioInstitucional(List<String> dominios) {
        this.dominios = dominios.stream().map(d -> d.trim().toLowerCase(Locale.ROOT)).filter(d -> !d.isEmpty()).toList();
    }

    /** Coincide con el dominio exacto o con un subdominio suyo (est.udea.edu.co), nunca con sufijos sueltos. */
    public boolean esInstitucional(String correoNormalizado) {
        int arroba = correoNormalizado.lastIndexOf('@');
        if (arroba < 1 || arroba == correoNormalizado.length() - 1) {
            return false;
        }
        String dominio = correoNormalizado.substring(arroba + 1);
        return dominios.stream().anyMatch(d -> dominio.equals(d) || dominio.endsWith("." + d));
    }
}
