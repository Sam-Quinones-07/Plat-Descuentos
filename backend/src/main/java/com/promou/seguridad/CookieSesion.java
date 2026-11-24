package com.promou.seguridad;

import com.promou.config.PromoUProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Deuda técnica 4: el token de sesión viaja en una cookie HttpOnly (JavaScript no la puede leer, así que un XSS no
 * la roba), Secure (solo HTTPS) y SameSite=Strict (el navegador no la envía desde otros sitios). El front llama a la
 * API por el mismo dominio (proxy de Vercel, Vite o nginx), por eso la cookie es de primera parte.
 */
@Component
public class CookieSesion {

    public static final String NOMBRE = "promou_sesion";

    private final PromoUProperties props;

    public CookieSesion(PromoUProperties props) {
        this.props = props;
    }

    /** Cookie de sesión del navegador (se borra al cerrarlo); la expiración por inactividad la valida el servidor. */
    public String crear(String token) {
        return base(token, null).toString();
    }

    public String borrar() {
        return base("", Duration.ZERO).toString();
    }

    public static String leer(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (NOMBRE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return c.getValue();
            }
        }
        return null;
    }

    private ResponseCookie base(String valor, Duration duracion) {
        ResponseCookie.ResponseCookieBuilder c = ResponseCookie.from(NOMBRE, valor)
                .httpOnly(true)
                .secure(props.seguridad().cookieSegura())
                .sameSite("Strict")
                .path("/api");
        return (duracion == null ? c : c.maxAge(duracion)).build();
    }
}
