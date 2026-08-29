-- ============================================================
--  ComercioControl API - Esquema de base de datos (SQLite)
--  Equivalente al modelo relacional de la evidencia GA6-220501096-AA2-EV01
--  Se ejecuta en cada arranque; es idempotente (CREATE TABLE IF NOT EXISTS)
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT    NOT NULL,
    usuario       TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    rol           TEXT    NOT NULL CHECK (rol IN ('administrador','vendedor','bodeguero')),
    activo        INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS clientes (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre         TEXT    NOT NULL,
    cedula         TEXT    UNIQUE,
    telefono       TEXT,
    email          TEXT,
    direccion      TEXT,
    activo         INTEGER NOT NULL DEFAULT 1,
    fecha_registro TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS proveedores (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nit          TEXT    NOT NULL UNIQUE,
    razon_social TEXT    NOT NULL,
    contacto     TEXT,
    telefono     TEXT,
    email        TEXT,
    activo       INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS productos (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo       TEXT    NOT NULL UNIQUE,
    nombre       TEXT    NOT NULL,
    categoria    TEXT,
    precio_costo REAL    NOT NULL DEFAULT 0 CHECK (precio_costo >= 0),
    precio_venta REAL    NOT NULL DEFAULT 0 CHECK (precio_venta >= 0),
    stock_actual INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INTEGER NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    activo       INTEGER NOT NULL DEFAULT 1,
    proveedor_id INTEGER REFERENCES proveedores(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_producto_categoria ON productos(categoria);

CREATE TABLE IF NOT EXISTS caja (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha       TEXT    NOT NULL,
    apertura    REAL    NOT NULL DEFAULT 0 CHECK (apertura >= 0),
    cierre      REAL    NOT NULL DEFAULT 0,
    ventas_dia  REAL    NOT NULL DEFAULT 0,
    diferencia  REAL    NOT NULL DEFAULT 0,
    observacion TEXT,
    usuario_id  INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    estado      TEXT    NOT NULL DEFAULT 'abierta' CHECK (estado IN ('abierta','cerrada'))
);

CREATE TABLE IF NOT EXISTS ventas (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    subtotal   REAL    NOT NULL DEFAULT 0,
    descuento  REAL    NOT NULL DEFAULT 0 CHECK (descuento >= 0),
    iva        REAL    NOT NULL DEFAULT 0,
    total      REAL    NOT NULL DEFAULT 0,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    cliente_id INTEGER          REFERENCES clientes(id)  ON DELETE SET NULL ON UPDATE CASCADE,
    caja_id    INTEGER          REFERENCES caja(id)      ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_venta_fecha ON ventas(fecha);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_id        INTEGER NOT NULL REFERENCES ventas(id)    ON DELETE CASCADE  ON UPDATE CASCADE,
    producto_id     INTEGER NOT NULL REFERENCES productos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario REAL    NOT NULL CHECK (precio_unitario >= 0)
);

CREATE TABLE IF NOT EXISTS movimientos (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id      INTEGER NOT NULL REFERENCES productos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    tipo             TEXT    NOT NULL CHECK (tipo IN ('entrada','salida','ajuste')),
    cantidad         INTEGER NOT NULL,
    stock_resultante INTEGER NOT NULL,
    fecha            TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    usuario_id       INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    observacion      TEXT
);

CREATE TABLE IF NOT EXISTS auditoria (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id     INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    accion         TEXT    NOT NULL,
    tabla_afectada TEXT,
    fecha          TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    detalle        TEXT
);
