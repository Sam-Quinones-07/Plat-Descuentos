-- PromoU - Release 1 (PostgreSQL). Los releases siguientes agregan sus tablas en migraciones nuevas (V3, V4…).

-- Herencia JOINED: usuario (datos comunes) + estudiante / establecimiento / administrador (datos propios).
CREATE TABLE usuario (
    id                 UUID PRIMARY KEY,
    correo             VARCHAR(254) NOT NULL,                 -- siempre en minúsculas
    contrasena_hash    VARCHAR(100) NOT NULL,
    correo_confirmado  BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_registro     TIMESTAMPTZ  NOT NULL
);
CREATE UNIQUE INDEX ux_usuario_correo ON usuario (correo);

CREATE TABLE estudiante (
    id                 UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    nombre             VARCHAR(80)  NOT NULL,
    apellido           VARCHAR(80)  NOT NULL,
    universidad        VARCHAR(120) NOT NULL,
    estado_verificado  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE administrador (
    id            UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    nombre        VARCHAR(80) NOT NULL,
    nivel_acceso  INTEGER     NOT NULL
);

CREATE TABLE solicitud_afiliacion (
    id                UUID PRIMARY KEY,
    estado            VARCHAR(20)  NOT NULL,                  -- PENDIENTE | APROBADA | RECHAZADA
    fecha_solicitud   TIMESTAMPTZ  NOT NULL,
    fecha_resolucion  TIMESTAMPTZ,
    observaciones     VARCHAR(500),
    administrador_id  UUID REFERENCES administrador (id),
    version           BIGINT       NOT NULL DEFAULT 0         -- bloqueo optimista: dos admins no resuelven a la vez
);
CREATE INDEX ix_solicitud_estado ON solicitud_afiliacion (estado, fecha_solicitud DESC);

CREATE TABLE establecimiento (
    id                      UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    solicitud_id            UUID         NOT NULL UNIQUE REFERENCES solicitud_afiliacion (id),
    nit                     VARCHAR(20)  NOT NULL,
    nit_normalizado         VARCHAR(20)  NOT NULL UNIQUE,     -- NIT sin guiones, para detectar duplicados
    nombre_comercial        VARCHAR(120) NOT NULL,
    razon_social            VARCHAR(160) NOT NULL,
    telefono                VARCHAR(20)  NOT NULL,
    direccion               VARCHAR(200) NOT NULL,
    representante_nombre    VARCHAR(120) NOT NULL,
    representante_documento VARCHAR(24)  NOT NULL,
    representante_correo    VARCHAR(254) NOT NULL
);

CREATE TABLE credencial_digital (
    id                 UUID PRIMARY KEY,
    estudiante_id      UUID        NOT NULL REFERENCES estudiante (id) ON DELETE CASCADE,
    codigo_qr          VARCHAR(40) NOT NULL UNIQUE,
    fecha_emision      TIMESTAMPTZ NOT NULL,
    fecha_vencimiento  TIMESTAMPTZ NOT NULL,
    revocada           BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX ix_credencial_estudiante ON credencial_digital (estudiante_id);

CREATE TABLE categoria (
    id      VARCHAR(30) PRIMARY KEY,
    nombre  VARCHAR(60) NOT NULL,
    orden   INTEGER     NOT NULL
);

CREATE TABLE descuento (
    id                   UUID PRIMARY KEY,
    establecimiento_id   UUID          NOT NULL REFERENCES establecimiento (id) ON DELETE CASCADE,
    categoria_id         VARCHAR(30)   NOT NULL REFERENCES categoria (id),
    beneficio            VARCHAR(255)  NOT NULL,
    condiciones          VARCHAR(2000) NOT NULL,
    fecha_inicio         DATE          NOT NULL,
    fecha_fin            DATE          NOT NULL,
    codigo               VARCHAR(40),
    ubicacion            VARCHAR(255)  NOT NULL,
    sitio_web            VARCHAR(500),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMPTZ   NOT NULL,
    fecha_actualizacion  TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_descuento_vigencia CHECK (fecha_fin >= fecha_inicio)
);
CREATE INDEX ix_descuento_directorio ON descuento (activo, fecha_fin);
CREATE INDEX ix_descuento_establecimiento ON descuento (establecimiento_id);
CREATE INDEX ix_descuento_categoria ON descuento (categoria_id);

CREATE TABLE sesion (
    id                UUID PRIMARY KEY,
    token_hash        VARCHAR(64) NOT NULL UNIQUE,
    usuario_id        UUID        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    rol               VARCHAR(20) NOT NULL,
    creada_en         TIMESTAMPTZ NOT NULL,
    ultima_actividad  TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_sesion_usuario ON sesion (usuario_id);

-- Control de intentos fallidos por correo (existan o no), para no filtrar qué correos están registrados.
CREATE TABLE intento_login (
    clave_hash      VARCHAR(64) PRIMARY KEY,           -- SHA-256 del correo normalizado
    intentos        INTEGER     NOT NULL,
    ventana_inicio  TIMESTAMPTZ,
    bloqueado_hasta TIMESTAMPTZ
);
