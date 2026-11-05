package com.promou.aplicacion;

import com.promou.config.PromoUProperties;
import com.promou.dominio.cuenta.SesionRepository;
import java.time.Clock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Limpieza de sesiones inactivas (HU 1.1.3 esc. 7). */
@Component
public class TareasProgramadas {

    private final SesionRepository sesiones;
    private final PromoUProperties props;
    private final Clock reloj;

    public TareasProgramadas(SesionRepository sesiones, PromoUProperties props, Clock reloj) {
        this.sesiones = sesiones;
        this.props = props;
        this.reloj = reloj;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    @Transactional
    public void limpiar() {
        sesiones.purgarInactivas(reloj.instant().minus(props.seguridad().inactividadSesion()));
    }
}
