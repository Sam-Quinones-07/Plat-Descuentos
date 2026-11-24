package com.promou.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promou.comun.LimitadorPeticiones;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Dos defensas para las peticiones que cambian datos:
 * - CSRF (deuda técnica 4): se exige la cabecera {@code X-PromoU}. Un formulario o una imagen de otro sitio no
 *   puede agregar cabeceras propias, y la cookie SameSite=Strict tampoco viaja desde otro sitio.
 * - Límite por IP en /api/auth/** (deuda técnica 6): frena la fuerza bruta y el envío masivo de correos.
 */
public class ProteccionPeticionesFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-PromoU";
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS");

    private final LimitadorPeticiones limitePorIp;
    private final ObjectMapper json;
    private final Clock reloj;

    public ProteccionPeticionesFilter(LimitadorPeticiones limitePorIp, ObjectMapper json, Clock reloj) {
        this.limitePorIp = limitePorIp;
        this.json = json;
        this.reloj = reloj;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean modifica = !METODOS_SEGUROS.contains(request.getMethod());
        if (modifica && request.getHeader(CABECERA) == null) {
            escribir(response, 403, "SOLICITUD_RECHAZADA", "La solicitud no proviene de la aplicación.");
            return;
        }
        if (modifica && request.getRequestURI().startsWith("/api/auth/")
                && !limitePorIp.permitir(ipCliente(request), reloj.instant())) {
            escribir(response, 429, "DEMASIADAS_SOLICITUDES",
                    "Hiciste demasiadas solicitudes seguidas. Espera un minuto e inténtalo de nuevo.");
            return;
        }
        chain.doFilter(request, response);
    }

    /** Vercel reemplaza X-Forwarded-For con la IP del cliente y Render agrega la suya al final: se toma la primera. */
    static String ipCliente(HttpServletRequest request) {
        String reenviada = request.getHeader("X-Forwarded-For");
        if (reenviada != null && !reenviada.isBlank()) {
            return reenviada.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void escribir(HttpServletResponse res, int status, String codigo, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), Map.of("codigo", codigo, "mensaje", mensaje));
    }
}
