package com.promou.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee la cookie de sesión, valida la sesión en BD y expone el principal. */
public class SesionFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_SESION_EXPIRADA = "promou.sesionExpirada";

    private final SesionService sesiones;

    public SesionFilter(SesionService sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = CookieSesion.leer(request);
        if (token != null) {
            SesionService.ResultadoSesion r = sesiones.autenticar(token);
            switch (r.estado()) {
                case ACTIVA -> {
                    UsuarioAutenticado p = r.principal();
                    var auth = new UsernamePasswordAuthenticationToken(p, null,
                            List.of(new SimpleGrantedAuthority(p.rol().authority())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                case EXPIRADA -> request.setAttribute(ATRIBUTO_SESION_EXPIRADA, Boolean.TRUE);
                case INEXISTENTE -> { }
            }
        }
        chain.doFilter(request, response);
    }
}
