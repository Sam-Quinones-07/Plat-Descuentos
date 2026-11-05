package com.promou.config;

import com.promou.comun.PoliticaContrasena;
import com.promou.comun.Texto;
import com.promou.dominio.usuario.Administrador;
import com.promou.dominio.usuario.UsuarioRepository;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador de la plataforma si no existe. No hay registro público de administradores.
 * La contraseña viene de ADMIN_CONTRASENA (en el perfil dev, la misma que usa el front de prueba).
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final PromoUProperties props;
    private final Clock reloj;

    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder encoder, PromoUProperties props, Clock reloj) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.props = props;
        this.reloj = reloj;
    }

    @Override
    public void run(ApplicationArguments args) {
        String correo = Texto.normalizarCorreo(props.admin().correo());
        if (usuarios.existsByCorreo(correo)) {
            return;
        }
        String contrasena = props.admin().contrasena();
        if (!PoliticaContrasena.cumple(contrasena)) {
            LOG.warn("No se creó el administrador: defina ADMIN_CONTRASENA con una contraseña que cumpla la política.");
            return;
        }
        usuarios.save(new Administrador(correo, encoder.encode(contrasena), props.admin().nombre(), 1, reloj.instant()));
        LOG.info("Administrador inicial creado: {}", correo);
    }
}
