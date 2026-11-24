package com.promou.integracion;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promou.aplicacion.notificacion.NotificacionService;
import com.promou.aplicacion.notificacion.NotificacionService.Mensaje;
import jakarta.servlet.http.Cookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prueba de integración (deuda técnica 7): API + seguridad + Flyway + PostgreSQL real en un contenedor.
 * Recorre registro, confirmación por enlace, aprobación, publicación y filtro por categoría (HU 2.2.3 esc. 1 y 3).
 * Se omite si no hay Docker (por ejemplo, en un PC sin Docker); en GitHub Actions sí corre.
 */
@SpringBootTest(properties = {
        "promou.admin.contrasena=Descuentos@04",
        "promou.seguridad.cookie-segura=false",
        "promou.seguridad.peticiones-auth-por-minuto-ip=1000"})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ApiIntegracionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final Pattern ENLACE = Pattern.compile("#confirmar/([A-Za-z0-9_-]+)");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private NotificacionService correo;

    private MockHttpServletRequestBuilder enviar(MockHttpServletRequestBuilder b, String cuerpo) {
        return b.contentType(MediaType.APPLICATION_JSON).header("X-PromoU", "1").content(cuerpo);
    }

    private String ultimoEnlace() {
        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(correo, atLeastOnce()).enviar(captor.capture());
        Matcher m = ENLACE.matcher(captor.getValue().cuerpo());
        if (!m.find()) {
            throw new AssertionError("El correo no trae el enlace de confirmación");
        }
        clearInvocations(correo);
        return m.group(1);
    }

    private Cookie login(String ruta, String correo, String contrasena) throws Exception {
        MvcResult r = mvc.perform(enviar(post(ruta), "{\"email\":\"" + correo + "\",\"password\":\"" + contrasena + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return r.getResponse().getCookie("promou_sesion");
    }

    @Test
    void flujoCompletoDelReleaseDos() throws Exception {
        // Estudiante: registro sin confirmar -> no puede entrar -> confirma por enlace -> entra
        mvc.perform(enviar(post("/api/auth/estudiantes/registro"), """
                {"firstName":"Laura","lastName":"Gómez","email":"laura@udea.edu.co","university":"UdeA",
                 "password":"Universitario@26","confirmation":"Universitario@26","acceptTerms":true}"""))
                .andExpect(status().isCreated());
        mvc.perform(enviar(post("/api/auth/login"), "{\"email\":\"laura@udea.edu.co\",\"password\":\"Universitario@26\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo", is("CORREO_NO_CONFIRMADO")));
        mvc.perform(enviar(post("/api/auth/confirmar"), "{\"token\":\"" + ultimoEnlace() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role", is("student")));
        Cookie estudiante = login("/api/auth/login", "laura@udea.edu.co", "Universitario@26");

        // Establecimiento: registro -> confirmación -> aprobación del administrador
        mvc.perform(enviar(post("/api/auth/establecimientos/registro"), """
                {"tradeName":"Burger Local","legalName":"Burger SAS","nit":"900123456-7",
                 "businessEmail":"contacto@burger.co","phone":"6045551234","address":"Cra 70",
                 "representativeName":"Carlos","document":"1000000001","email":"carlos@burger.co",
                 "password":"Hamburguesa@26","confirmation":"Hamburguesa@26","acceptTerms":true}"""))
                .andExpect(status().isCreated());
        mvc.perform(enviar(post("/api/auth/confirmar"), "{\"token\":\"" + ultimoEnlace() + "\"}"))
                .andExpect(status().isOk());
        Cookie admin = login("/api/auth/login", "admin@plataforma.com", "Descuentos@04");
        JsonNode pendientes = json.readTree(mvc.perform(get("/api/admin/afiliaciones?estado=pending").cookie(admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        mvc.perform(enviar(post("/api/admin/afiliaciones/" + pendientes.get(0).get("id").asText() + "/aprobar"), "{}")
                .cookie(admin)).andExpect(status().isOk());

        // Publica 5 descuentos, 2 de restaurantes
        Cookie comercio = login("/api/auth/establecimientos/login", "contacto@burger.co", "Hamburguesa@26");
        String[] categorias = {"restaurants", "restaurants", "technology", "fashion", "stores"};
        for (int i = 0; i < categorias.length; i++) {
            mvc.perform(enviar(post("/api/establecimientos/me/descuentos"), """
                    {"benefit":"Beneficio %d","conditions":"Condiciones","startDate":"2020-01-01",
                     "endDate":"2099-12-31","location":"Cra 70","category":"%s"}""".formatted(i, categorias[i]))
                    .cookie(comercio)).andExpect(status().isCreated());
        }

        // HU 2.2.3: con la categoría solo aparecen los 2; sin ella, los 5
        mvc.perform(get("/api/descuentos?categoria=restaurants").cookie(estudiante))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems", is(2)));
        mvc.perform(get("/api/descuentos").cookie(estudiante))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems", is(5)));

        // Sin la cabecera X-PromoU no se aceptan cambios (protección CSRF)
        mvc.perform(post("/api/auth/logout").cookie(estudiante)).andExpect(status().isForbidden());
    }
}
