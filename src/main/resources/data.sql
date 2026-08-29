-- ============================================================
--  ComercioControl API - Datos iniciales (semilla)
--  INSERT OR IGNORE: se cargan una sola vez, no duplican al reiniciar
--  Contrasenas almacenadas con hash SHA-256
-- ============================================================

INSERT OR IGNORE INTO usuarios (id, nombre, usuario, password_hash, rol) VALUES
(1, 'Ivan Manuel Rodriguez David', 'ivan.admin',    '8d90ed647b948fa80c3c9bbf5316c78f151723f52fb9d6101f818af8afff69ec', 'administrador'),
(2, 'Laura Gomez Diaz',            'laura.ventas',  '96dd549c187c49903ad9df1c1fbf055010eb2c111fe62a846f3f062507434e3b', 'vendedor'),
(3, 'Carlos Perez Ruiz',           'carlos.bodega', '61ca105a2751fa51acb1661a41aaa8ed5ea0a933db7c123c81b4d1c391253245', 'bodeguero');

INSERT OR IGNORE INTO proveedores (id, nit, razon_social, contacto, telefono, email) VALUES
(1, '900123456-1', 'Distribuidora La Economia S.A.S.', 'Marta Rios',  '3104567890', 'ventas@laeconomia.com'),
(2, '901987654-2', 'Alimentos del Huila Ltda.',        'Pedro Salas', '3159876543', 'pedidos@alimentoshuila.com'),
(3, '830555777-3', 'Aseo Total S.A.',                  'Julia Mora',  '3201112233', 'contacto@aseototal.com');

INSERT OR IGNORE INTO clientes (id, nombre, cedula, telefono, email, direccion) VALUES
(1, 'Consumidor final',     NULL,         NULL,         NULL,                  NULL),
(2, 'Ana Maria Torres',     '1075001001', '3001234567', 'ana.torres@mail.com', 'Cra 5 # 10-23'),
(3, 'Jose Luis Fernandez',  '1075002002', '3017654321', 'jose.fdez@mail.com',  'Cll 8 # 4-56'),
(4, 'Diana Carolina Munoz', '1075003003', '3025557788', 'diana.m@mail.com',    'Av 26 # 15-40');

INSERT OR IGNORE INTO productos (id, codigo, nombre, categoria, precio_costo, precio_venta, stock_actual, stock_minimo, proveedor_id) VALUES
(1, 'P-001', 'Arroz x 500 g',        'Granos',    1800,  2500, 120, 20, 2),
(2, 'P-002', 'Aceite girasol 1 L',   'Aceites',   9500, 12900,   4, 10, 2),
(3, 'P-003', 'Leche entera 1 L',     'Lacteos',   3600,  4900,  45, 15, 1),
(4, 'P-004', 'Azucar x 1 kg',        'Granos',    2900,  3800,  80, 25, 2),
(5, 'P-005', 'Jabon en polvo 500 g', 'Aseo',      4200,  6500,  30, 10, 3),
(6, 'P-006', 'Gaseosa 1.5 L',        'Bebidas',   3200,  4800,  60, 15, 1),
(7, 'P-007', 'Cafe molido 250 g',    'Abarrotes', 6800,  9200,   8, 12, 2);
