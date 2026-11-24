-- Release 2: confirmación de correo y restablecimiento (HU 1.1.2, 1.2.2, 1.3.2),
-- descuentos guardados (HU 2.2.4), reseñas (HU 3.1.1, 3.1.2) y aceptación de términos (sección 11.1).

-- Enlaces de un solo uso. En la base solo queda el hash SHA-256 del token, nunca el token.
CREATE TABLE token_verificacion (
    id          UUID PRIMARY KEY,
    usuario_id  UUID        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo        VARCHAR(20) NOT NULL,                  -- CONFIRMACION | RESTABLECIMIENTO
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    creado_en   TIMESTAMPTZ NOT NULL,
    expira_en   TIMESTAMPTZ NOT NULL,
    usado_en    TIMESTAMPTZ
);
CREATE INDEX ix_token_usuario_tipo ON token_verificacion (usuario_id, tipo);

CREATE TABLE descuento_guardado (
    id             UUID PRIMARY KEY,
    estudiante_id  UUID        NOT NULL REFERENCES estudiante (id) ON DELETE CASCADE,
    descuento_id   UUID        NOT NULL REFERENCES descuento (id) ON DELETE CASCADE,
    guardado_en    TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_guardado UNIQUE (estudiante_id, descuento_id)     -- HU 2.2.4 esc. 4: sin duplicados
);

CREATE TABLE resena (
    id             UUID PRIMARY KEY,
    estudiante_id  UUID         NOT NULL REFERENCES estudiante (id) ON DELETE CASCADE,
    descuento_id   UUID         NOT NULL REFERENCES descuento (id) ON DELETE CASCADE,
    texto          VARCHAR(500) NOT NULL,
    publicada_en   TIMESTAMPTZ  NOT NULL,
    retirada       BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT ux_resena UNIQUE (estudiante_id, descuento_id)        -- una reseña por estudiante y descuento
);
CREATE INDEX ix_resena_descuento ON resena (descuento_id, publicada_en DESC);

-- Prueba de la autorización de tratamiento de datos (Ley 1581 de 2012). Nulo en cuentas anteriores al Release 2.
ALTER TABLE usuario ADD COLUMN terminos_aceptados_en TIMESTAMPTZ;
ALTER TABLE usuario ADD COLUMN terminos_version VARCHAR(20);
