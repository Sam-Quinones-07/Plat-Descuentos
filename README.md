# PromoU – Release 1

Solo las 11 historias de usuario *Must* del Release 1 (mapa de historias), con su backend (Spring Boot + PostgreSQL),
su frontend (React) y el despliegue en Docker.

```
promou-release1/
├── backend/            Spring Boot + PostgreSQL (Flyway)
├── frontend/           React + Vite (servido con nginx)
└── docker-compose.yml  db + backend + frontend
```

## Despliegue (producción)

| Parte | Plataforma | URL |
|---|---|---|
| Frontend | Vercel (Root Directory `frontend`, `vercel.json`) | https://plat-descuentos.vercel.app |
| Backend + PostgreSQL | Render, Blueprint `render.yaml` | https://promou-backend.onrender.com |

- Vercel: variable `VITE_API_URL=https://promou-backend.onrender.com`.
- Render: `ADMIN_CONTRASENA` (administrador `admin@plataforma.com`) y `CORS_ORIGENES=https://plat-descuentos.vercel.app`.
- Cada push a `main` redespliega ambos automáticamente.
- Plan gratuito de Render: el backend se suspende tras 15 min sin uso (la primera petición tarda ~50 s) y la base gratuita vence a los 30 días.


### Recorrido de prueba

1. **Estudiante:** *Regístrate* con un correo `@udea.edu.co` → inicia sesión → **Descuentos** (vacío) → **Mi perfil**
   (identificación con QR) → **Cerrar sesión**.
2. **Establecimiento:** *Solicita la afiliación* → intenta iniciar sesión: avisa que la solicitud está **pendiente**.
3. **Administrador:** inicia sesión → **Revisar** la solicitud → **Aprobar**.
4. **Establecimiento:** inicia sesión → **Publicar descuento** (fechas que incluyan hoy) → aparece en *Mis descuentos*.
5. **Estudiante:** el descuento aparece en el directorio → **Ver detalle**.

## HU incluidas y endpoints

| HU | Endpoint |
|---|---|
| 1.1.1 Registrar estudiante | `POST /api/auth/estudiantes/registro` |
| 1.1.3 Iniciar sesión estudiante | `POST /api/auth/login` · `GET /api/auth/me` |
| 1.1.4 Cerrar sesión estudiante | `POST /api/auth/logout` |
| 1.2.1 Registrar establecimiento | `POST /api/auth/establecimientos/registro` |
| 1.2.3 Revisar solicitud de afiliación | `GET /api/admin/afiliaciones?estado=pending\|reviewed` · `GET …/{id}` · `POST …/{id}/aprobar` · `POST …/{id}/rechazar` (notifica al representante) |
| 1.2.4 Iniciar sesión establecimiento | `POST /api/auth/establecimientos/login` |
| 1.2.5 Cerrar sesión establecimiento | `POST /api/auth/logout` |
| 1.4.1 Generar identificación digital | `GET /api/estudiantes/me/credencial` |
| 2.1.1 Publicar descuento | `POST /api/establecimientos/me/descuentos` · `GET` (tablero "Mis descuentos") |
| 2.2.1 Visualizar directorio | `GET /api/descuentos` |
| 2.2.2 Consultar detalle | `GET /api/descuentos/{id}` (410 si ya no está disponible; el front lo abre en `#descuento/{id}`) |

Controladores (nombres del documento de diseño, sección 5.1): `AuthController` (1.1.1, 1.1.3, 1.1.4, 1.2.1, 1.2.4, 1.2.5),
`AfiliacionController` (1.2.3), `CredencialController` (1.4.1), `DescuentoController` (2.1.1) y `DirectorioController` (2.2.1, 2.2.2).

Sesión: `Authorization: Bearer <token>` (token opaco guardado en el servidor; cerrar sesión lo elimina; expira a los 30 min
de inactividad). Cinco fallos de login en 15 min bloquean la cuenta 15 min. Errores: `{ codigo, mensaje, campos }`.

## Patrones (solo los de estas HU)

| Patrón | Dónde | HU |
|---|---|---|
| GRASP Controller | `GestionDescuentosService` coordina; `EstablecimientoController` solo traduce HTTP | 2.1.1 |
| GRASP Information Expert | `Descuento.validar()`, `validarVigencia()`, `estaDisponible()` | 2.1.1, 2.2.1 |
| GRASP Creator | `Establecimiento.crearDescuento()`, `Estudiante.generarCredencial()` | 2.1.1, 1.4.1 |
| **Builder** | `DescuentoBuilder` | 2.1.1 |
| **Adapter** | `NotificacionService` (interfaz objetivo) ← `CorreoSmtpAdapter` (SMTP) / `NotificacionConsolaAdapter` (local) | 1.2.3 esc. 6 |
| Low Coupling / High Cohesion / Pure Fabrication / Indirection | repositorios Spring Data (`DescuentoRepository`…), un servicio por caso de uso | todas |
| State | `EstadoSolicitud` (pendiente / aprobada / rechazada): transiciones y acceso al login | 1.2.3, 1.2.4 |
| Observer | `EstudianteVerificado` → `CredencialService` emite la identificación; `SolicitudRevisada` → `NotificacionAfiliacionListener` (después del commit) | 1.1.1 → 1.4.1, 1.2.3 |
| Antipatrones evitados | God class (servicios separados), Tight coupling (interfaces de repositorio), condicionales (State en vez de `if/switch`) | |
## Verificación

Backend: `mvn test` con **27 pruebas unitarias** (dominio, contraseñas, bloqueo, identificación, notificación). Arranque real
contra PostgreSQL 16: migraciones Flyway y `ddl-auto: validate` correctos. Front: `tsc --noEmit` y `vite build` sin errores.
Recorrido manual completo en local (estudiante, establecimiento y administrador).
