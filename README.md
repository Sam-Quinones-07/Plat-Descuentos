# PromoU – Release 1

Solo las 11 historias de usuario *Must* del Release 1 (mapa de historias), con su backend (Spring Boot + PostgreSQL),
su frontend (React) y el despliegue en Docker.

```
promou-release1/
├── backend/            Spring Boot + PostgreSQL (Flyway)
├── frontend/           React + Vite (servido con nginx)
└── docker-compose.yml  db + backend + frontend
```

## Cómo correrlo en local (sin Docker)

Requisitos: JDK 21, Node.js 20+, PostgreSQL 16 y Maven (o `backend/run-local.cmd`, que lo busca en `%USERPROFILE%\tools`).

1. Base de datos (una sola vez), con `psql -U postgres`:
   ```sql
   CREATE ROLE promou LOGIN PASSWORD 'promou';
   CREATE DATABASE promou OWNER promou;
   ```
2. Backend (puerto 8080, perfil `dev`): `backend\run-local.cmd`
   (equivale a `mvn spring-boot:run -Dspring-boot.run.profiles=dev`). Flyway crea las tablas al arrancar.
3. Frontend (puerto 8443): `cd frontend` → `npm install` → `npm run dev`. Vite reenvía `/api` al backend.
4. Abre http://localhost:8443. Administrador: `admin@plataforma.com` / `Descuentos@04`.

Correo (HU 1.2.3 esc. 6): sin `SMTP_HOST` el aviso al representante se escribe en el log del backend
("Notificación simulada"). Con `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` y `CORREO_REMITENTE` se envía por SMTP.

## Cómo correrlo con Docker

Con Docker Desktop abierto, desde esta carpeta:

```bash
docker compose up --build        # la primera vez tarda (descarga dependencias)
```

- App: http://localhost:8443  ·  API directa: http://localhost:8080
- Administrador: `admin@plataforma.com` / `Descuentos@04`
- Si tienes la versión completa corriendo, detenla antes (`docker compose down`): usan los mismos puertos.
- Empezar de cero (borra la base): `docker compose down -v`

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

**No incluidos a propósito** (pertenecen a otros releases): confirmación de registro por correo (Release 2, reutilizará el mismo
Adapter), **Strategy** (validar identificación / código, Release 3), Command, Facade y Decorator (opcionales en el documento).

## Decisiones de este release

- **Sin correos:** no hay confirmación por enlace (Release 2). El estudiante queda **verificado al registrarse**, porque su correo
  pertenece a un dominio institucional (configurable en `promou.seguridad.dominios-institucionales`), y su identificación se genera
  en ese momento. El establecimiento queda pendiente hasta que el administrador lo apruebe.
- **Notificación de la revisión (HU 1.2.3 esc. 6):** al aprobar o rechazar se envía un aviso al correo del representante
  (Adapter); además, el representante ve el resultado al iniciar sesión (pendiente / rechazada / aprobada).
- **Identificación digital (HU 1.4.1):** el QR es un código QR real (librería `qrcode`) con el identificador único.
- **Código del descuento:** se guarda al publicar, pero el estudiante lo verá con "Copiar código" (Release 2).
- **Categorías:** se eligen al publicar; el filtro del directorio es del Release 2.
- **Esquema:** la migración `V1` solo crea las tablas de este release; los siguientes agregan las suyas (reseñas, guardados, enlaces).
- Los mensajes de conflicto del registro de establecimientos son genéricos por diseño (HU 1.2.1 esc. 3 y 5).

## Verificación

Backend: `mvn test` con **27 pruebas unitarias** (dominio, contraseñas, bloqueo, identificación, notificación). Arranque real
contra PostgreSQL 16: migraciones Flyway y `ddl-auto: validate` correctos. Front: `tsc --noEmit` y `vite build` sin errores.
Recorrido manual completo en local (estudiante, establecimiento y administrador).
