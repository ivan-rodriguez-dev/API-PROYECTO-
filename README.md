<div align="center">

# ComercioControl API

**API REST del proyecto formativo ComercioControl** — punto de venta e inventario para comercio minorista.

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-3-003B57?logo=sqlite&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3-85EA2D?logo=swagger&logoColor=black)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)

</div>

> **Evidencia:** GA7-220501096-AA5-EV04_IVO — API del proyecto
> **Aprendiz:** Iván Manuel Rodríguez David · **Ficha:** 3235886 · **Programa:** ADSO — SENA
> **Proyecto base:** [ComercioControl](https://github.com/JuanMa3132/Comercio-ControlV1) (aplicación de escritorio JavaFX)

---

## 1. ¿Qué es y por qué existe?

ComercioControl nació como una aplicación **de escritorio** (JavaFX + SQLite): la interfaz y la base de
datos viven en el mismo equipo. En el *Plan de desarrollo* (evidencia GA7-AA1-EV02) se definió que la
**fase 2 del proyecto es una aplicación móvil en Flutter**, y una app móvil no puede abrir directamente
el archivo SQLite del computador de la tienda.

Esta API resuelve ese punto: expone por **HTTP** los mismos procesos de negocio del sistema, de forma
que puedan consumirlos la aplicación de escritorio, el módulo frontend web (evidencia GA6-AA4-EV01),
la futura app móvil o cualquier otro cliente. La arquitectura pasa de monolítica de escritorio a
**cliente-servidor**.

```
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  App móvil   │   │   Frontend   │   │  Escritorio  │
│   Flutter    │   │  HTML/CSS/JS │   │   JavaFX     │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │      HTTP / JSON │                  │
       └──────────────────┼──────────────────┘
                          ▼
             ┌────────────────────────┐
             │   ComercioControl API  │
             │  Spring Boot · JWT     │
             │  Controller → Service  │
             │      → Repository      │
             └───────────┬────────────┘
                         ▼
                ┌─────────────────┐
                │ SQLite (archivo)│
                └─────────────────┘
```

## 2. Trazabilidad con los requerimientos del proyecto

| Requerimiento funcional | Historia de usuario | Servicio web que lo implementa |
|---|---|---|
| RF-01 Autenticación y control de acceso por rol | *Como usuario quiero iniciar sesión para acceder solo a mis módulos* | `POST /api/auth/login`, `GET /api/auth/perfil` |
| RF-02 Gestión del catálogo de productos | *Como bodeguero quiero administrar los productos del inventario* | `GET/POST/PUT/DELETE /api/productos` |
| RF-03 Alerta de stock crítico | *Como bodeguero quiero saber qué productos debo reponer* | `GET /api/productos/stock-critico` |
| RF-04 Gestión de clientes | *Como vendedor quiero registrar a mis clientes* | `GET/POST/PUT/DELETE /api/clientes` |
| RF-05 Gestión de proveedores | *Como bodeguero quiero registrar a los proveedores* | `GET/POST/PUT/DELETE /api/proveedores` |
| RF-06 Registro de ventas (POS) | *Como vendedor quiero registrar una venta y que descuente el stock* | `POST /api/ventas` |
| RF-07 Consulta de ventas | *Como administrador quiero consultar las ventas del período* | `GET /api/ventas`, `GET /api/ventas/{id}` |
| RF-08 Movimientos de inventario | *Como bodeguero quiero registrar entradas, salidas y ajustes* | `GET/POST /api/movimientos` |
| RF-09 Caja diaria | *Como vendedor quiero abrir y cerrar caja con su cuadre* | `POST /api/caja/apertura`, `POST /api/caja/cierre`, `GET /api/caja/estado` |
| RF-10 Reportes e indicadores | *Como administrador quiero ver los indicadores del negocio* | `GET /api/reportes/*` |
| RF-11 Auditoría | *Como administrador quiero saber quién hizo cada operación* | `GET /api/auditoria` |
| RF-12 Administración de usuarios | *Como administrador quiero crear y desactivar usuarios* | `GET/POST /api/usuarios`, `PUT /api/usuarios/{id}/estado` |

## 3. Tecnologías

| Componente | Tecnología | Justificación |
|---|---|---|
| Lenguaje | Java 17 (LTS) | Es el lenguaje ratificado para el proyecto; permite reutilizar el modelo de dominio |
| Framework web | Spring Boot 3.3.5 | Estándar de la industria para servicios REST en Java |
| Persistencia | Spring JDBC (`JdbcTemplate`) + SQLite | Mantiene el patrón **DAO con SQL parametrizado** ya usado en el escritorio |
| Seguridad | JWT (jjwt 0.12.6) | Autenticación sin estado, adecuada para clientes móviles |
| Validación | Bean Validation (Jakarta) | Validaciones declarativas sobre los DTO de entrada |
| Documentación | springdoc-openapi 2.6.0 | Genera OpenAPI 3 y Swagger UI a partir del propio código |
| Pruebas | JUnit 5 + MockMvc | Pruebas automáticas de los endpoints |
| Construcción | Maven | Mismo gestor de dependencias del proyecto base |

## 4. Arquitectura en capas

```
src/main/java/com/comerciocontrol/api/
├── ApiApplication.java      # Arranque de la aplicación
├── config/                  # OpenAPI, CORS y registro del interceptor
├── security/                # JWT, hash de contraseñas, control de acceso por rol
├── exception/               # Excepciones propias y manejador global de errores
├── model/                   # Entidades del dominio (records)
├── dto/                     # Objetos de entrada/salida con sus validaciones
├── repository/              # Acceso a datos: SQL parametrizado (patrón DAO)
├── service/                 # Reglas de negocio y transacciones
└── controller/              # Endpoints REST
```

| Capa | Responsabilidad | Regla que se respeta |
|---|---|---|
| `controller` | Recibe la petición HTTP, valida el formato y devuelve el código de estado | **No contiene lógica de negocio ni SQL** |
| `service` | Aplica las reglas de negocio y delimita las transacciones | Es la única capa que decide si una operación es válida |
| `repository` | Ejecuta el SQL con `PreparedStatement` | **Es la única capa que conoce la base de datos** |

Es exactamente la misma separación de responsabilidades del proyecto de escritorio
(`controller` → `dao` → base de datos), trasladada al mundo web.

## 5. Reglas de negocio implementadas

1. **No se vende sin caja abierta.** `POST /api/ventas` responde `409` si no hay una jornada de caja abierta.
2. **El stock nunca queda negativo.** El descuento se hace con un `UPDATE ... WHERE stock_actual >= ?`;
   si no afecta ninguna fila se lanza el error de stock insuficiente y **se revierte toda la venta**.
3. **La venta es una transacción atómica**: cabecera + detalle + descuento de stock + movimiento de
   salida + auditoría. Si algo falla, la base de datos queda como estaba.
4. **Los importes los calcula el servidor.** El cliente envía solo `productoId` y `cantidad`; el precio
   se toma de la base de datos. Fórmula: `base = subtotal − descuento`, `IVA = base × 19 %`, `total = base + IVA`.
5. **Un descuento no puede superar el subtotal.**
6. **El precio de venta no puede ser menor que el precio de costo.**
7. **Solo puede haber una caja abierta a la vez**; el cierre calcula
   `diferencia = efectivo contado − (apertura + ventas del día)`.
8. **Códigos, cédulas y NIT son únicos.**
9. **Las bajas son lógicas** (`activo = 0`) para no perder el historial referenciado por las ventas.
10. **Toda operación que modifica datos deja traza de auditoría** con el usuario responsable.

## 6. Seguridad y control de acceso

La autenticación es por **JWT**. `POST /api/auth/login` devuelve un token que debe enviarse en las
demás peticiones como `Authorization: Bearer <token>`. Las contraseñas se almacenan con **SHA-256**
(se mantiene la verificación de hashes MD5 heredados por compatibilidad con bases de datos antiguas).

Se replica la matriz de permisos de la aplicación de escritorio:

| Recurso | administrador | vendedor | bodeguero |
|---|:---:|:---:|:---:|
| Productos (consulta) | ✅ | ✅ | ✅ |
| Productos (crear / editar) | ✅ | ❌ | ✅ |
| Productos (dar de baja) | ✅ | ❌ | ❌ |
| Clientes | ✅ | ✅ | ❌ |
| Proveedores | ✅ | ❌ | ✅ |
| Ventas (POS) | ✅ | ✅ | ❌ |
| Movimientos de stock | ✅ | ❌ | ✅ |
| Caja | ✅ | ✅ | ❌ |
| Reportes | ✅ | ✅ | ✅ |
| Usuarios y auditoría | ✅ | ❌ | ❌ |

**Usuarios de prueba** (se crean solos la primera vez que arranca la API):

| Usuario | Contraseña | Rol |
|---|---|---|
| `ivan.admin` | `Admin2026*` | administrador |
| `laura.ventas` | `Venta2026*` | vendedor |
| `carlos.bodega` | `Bodega2026*` | bodeguero |

## 7. Manejo de errores

Todos los errores devuelven el mismo formato JSON, con el código HTTP que corresponde:

```json
{
  "fecha": "2026-08-29T15:42:10.123",
  "estado": 409,
  "error": "Regla de negocio",
  "mensaje": "Stock insuficiente de P-002 - Aceite girasol 1 L. Disponible: 4, solicitado: 10",
  "ruta": "/api/ventas"
}
```

| Código | Cuándo se devuelve |
|---|---|
| `200` / `201` / `204` | Operación exitosa (consulta / creación / eliminación) |
| `400` | Datos inválidos o JSON mal formado (incluye el detalle campo por campo) |
| `401` | Falta el token, es inválido o expiró |
| `403` | El rol del usuario no tiene permiso sobre ese recurso |
| `404` | El recurso solicitado no existe |
| `409` | Una regla de negocio o una restricción de la base de datos impide la operación |
| `500` | Error inesperado (nunca se expone la traza de Java) |

## 8. Cómo ejecutar

**Requisitos:** JDK 17 o superior y Maven 3.8+.

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080`. La base de datos `comerciocontrol.db` se crea sola en el
directorio de ejecución con el esquema y los datos de prueba.

Para generar el ejecutable:

```bash
mvn clean package
```

```bash
java -jar target/comerciocontrol-api.jar
```

## 9. Documentación interactiva

Con la API en ejecución:

| Recurso | URL |
|---|---|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| Especificación OpenAPI 3 (JSON) | http://localhost:8080/v3/api-docs |
| Estado del servicio | http://localhost:8080/api/health |

Swagger UI permite probar cada endpoint desde el navegador: se hace login, se pulsa **Authorize**, se
pega el token y ya se pueden ejecutar el resto de operaciones.

## 10. Catálogo de endpoints

| # | Método | Ruta | Descripción | Roles |
|---|---|---|---|---|
| 1 | `GET` | `/` | Índice de la API con enlaces a la documentación | público |
| 2 | `GET` | `/api/health` | Estado de la API y de la base de datos | público |
| 3 | `POST` | `/api/auth/login` | Iniciar sesión y obtener el token JWT | público |
| 4 | `GET` | `/api/auth/perfil` | Datos del usuario autenticado | todos |
| 5 | `GET` | `/api/productos` | Listar productos (filtros `q`, `categoria`) | todos |
| 6 | `GET` | `/api/productos/{id}` | Consultar un producto | todos |
| 7 | `GET` | `/api/productos/stock-critico` | Productos que requieren reposición | todos |
| 8 | `GET` | `/api/productos/categorias` | Categorías existentes | todos |
| 9 | `POST` | `/api/productos` | Crear un producto | admin, bodeguero |
| 10 | `PUT` | `/api/productos/{id}` | Actualizar un producto | admin, bodeguero |
| 11 | `DELETE` | `/api/productos/{id}` | Dar de baja un producto | admin |
| 12 | `GET` | `/api/clientes` | Listar clientes (filtro `q`) | admin, vendedor |
| 13 | `GET` | `/api/clientes/{id}` | Consultar un cliente | admin, vendedor |
| 14 | `POST` | `/api/clientes` | Registrar un cliente | admin, vendedor |
| 15 | `PUT` | `/api/clientes/{id}` | Actualizar un cliente | admin, vendedor |
| 16 | `DELETE` | `/api/clientes/{id}` | Dar de baja un cliente | admin |
| 17 | `GET` | `/api/proveedores` | Listar proveedores | admin, bodeguero |
| 18 | `GET` | `/api/proveedores/{id}` | Consultar un proveedor | admin, bodeguero |
| 19 | `POST` | `/api/proveedores` | Registrar un proveedor | admin, bodeguero |
| 20 | `PUT` | `/api/proveedores/{id}` | Actualizar un proveedor | admin, bodeguero |
| 21 | `DELETE` | `/api/proveedores/{id}` | Dar de baja un proveedor | admin |
| 22 | `GET` | `/api/ventas` | Listar ventas (`desde`, `hasta`) | admin, vendedor |
| 23 | `GET` | `/api/ventas/{id}` | Consultar una venta con su detalle | admin, vendedor |
| 24 | `POST` | `/api/ventas` | **Registrar una venta (transacción POS)** | admin, vendedor |
| 25 | `GET` | `/api/movimientos` | Kardex (`productoId`, `tipo`) | admin, bodeguero |
| 26 | `GET` | `/api/movimientos/{id}` | Consultar un movimiento | admin, bodeguero |
| 27 | `POST` | `/api/movimientos` | Registrar entrada, salida o ajuste | admin, bodeguero |
| 28 | `GET` | `/api/caja/estado` | ¿Hay caja abierta? | admin, vendedor |
| 29 | `GET` | `/api/caja` | Historial de jornadas de caja | admin, vendedor |
| 30 | `POST` | `/api/caja/apertura` | Abrir la caja | admin, vendedor |
| 31 | `POST` | `/api/caja/cierre` | Cerrar la caja y calcular el cuadre | admin, vendedor |
| 32 | `GET` | `/api/reportes/ventas` | Resumen de ventas del período | todos |
| 33 | `GET` | `/api/reportes/mas-vendidos` | Ranking de productos más vendidos | todos |
| 34 | `GET` | `/api/reportes/inventario` | Valorización del inventario | todos |
| 35 | `GET` | `/api/reportes/stock-critico` | Productos por reponer | todos |
| 36 | `GET` | `/api/usuarios` | Listar usuarios | admin |
| 37 | `POST` | `/api/usuarios` | Crear un usuario | admin |
| 38 | `PUT` | `/api/usuarios/{id}/estado` | Activar o desactivar un usuario | admin |
| 39 | `GET` | `/api/auditoria` | Traza de auditoría | admin |


### 10.1 Contrato detallado de los servicios base

El catálogo anterior lista las 39 rutas. Esta sección documenta, para los servicios que conforman la
base de cualquier aplicación —autenticación y un módulo CRUD completo—, los **parámetros de entrada**
y las **respuestas esperadas**. El contrato completo de las 39 rutas, con sus esquemas, está publicado
en Swagger UI (`/swagger-ui.html`) y en el documento OpenAPI (`/v3/api-docs`).

#### Registro de usuario

| | |
|---|---|
| **Método y ruta** | `POST /api/usuarios` |
| **Autorización** | `Bearer <token>` · rol `administrador` |
| **Cuerpo** | `nombre` (texto, obligatorio) · `usuario` (texto, 4–50 caracteres, único) · `password` (texto, mínimo 8 caracteres) · `rol` (`administrador` \| `vendedor` \| `bodeguero`) |

| Respuesta | Cuándo | Cuerpo |
|---|---|---|
| `201 Created` | El usuario se registró | Usuario creado con su `id`. **Nunca devuelve la contraseña** |
| `400 Bad Request` | Algún campo no pasa la validación | `detalles` con el motivo campo por campo |
| `409 Conflict` | El nombre de usuario ya existe | `mensaje` explicando el conflicto |
| `403 Forbidden` | Quien llama no es administrador | Error uniforme |

#### Inicio de sesión

| | |
|---|---|
| **Método y ruta** | `POST /api/auth/login` |
| **Autorización** | Pública |
| **Cuerpo** | `usuario` (texto, obligatorio) · `password` (texto, obligatorio) |

| Respuesta | Cuándo | Cuerpo |
|---|---|---|
| `200 OK` | Credenciales válidas | `token` (JWT), `tipo` (`Bearer`), `expiraEnSegundos` y los datos del `usuario` |
| `401 Unauthorized` | Usuario inexistente, inactivo o contraseña incorrecta | Error uniforme, sin revelar cuál de los dos falló |
| `400 Bad Request` | Falta alguno de los dos campos | `detalles` por campo |

La contraseña se guarda con hash **SHA-256**; el token se firma con HS512 y caduca a las 8 horas.

#### CRUD de productos

| Operación | Método y ruta | Entrada | Respuesta esperada |
|---|---|---|---|
| **Crear** | `POST /api/productos` | `codigo` (único, ≤30) · `nombre` · `categoria` · `precioCosto` ≥ 0 · `precioVenta` ≥ 0 · `stockActual` ≥ 0 · `stockMinimo` ≥ 0 · `proveedorId` (opcional) | `201` con el producto y su `id` en la cabecera `Location`. `409` si el código se repite o si el precio de venta es menor al costo |
| **Consultar** | `GET /api/productos/{id}` | `id` en la ruta | `200` con el producto, su `estado` (`OK` \| `BAJO` \| `CRITICO`) y la `utilidadUnitaria` calculada. `404` si no existe |
| **Listar** | `GET /api/productos` | `q` (texto libre) y `categoria`, ambos opcionales | `200` con el arreglo de productos activos que coinciden |
| **Actualizar** | `PUT /api/productos/{id}` | Mismos campos que el alta | `200` con el producto ya actualizado. `404` si no existe |
| **Eliminar** | `DELETE /api/productos/{id}` | `id` en la ruta | `204` sin cuerpo. Es una **baja lógica**: el producto queda con `activo: false` y sale del catálogo, pero se conserva para no romper la trazabilidad de las ventas que lo referencian |

Todas las rutas de escritura exigen rol `administrador` o `bodeguero`; un `vendedor` recibe `403`.

#### Formato uniforme de error

Cualquier fallo, sea de validación o de negocio, responde con la misma estructura, de modo que un
cliente puede tratarlos con un solo bloque de código:

```json
{
  "fecha": "2026-09-04T19:10:13",
  "estado": 400,
  "error": "Datos invalidos",
  "mensaje": "La peticion contiene campos que no cumplen las validaciones",
  "ruta": "/api/usuarios",
  "detalles": {
    "usuario": "El usuario debe tener entre 4 y 50 caracteres",
    "password": "La contrasena debe tener al menos 8 caracteres",
    "rol": "El rol debe ser 'administrador', 'vendedor' o 'bodeguero'"
  }
}
```

El campo `detalles` solo aparece cuando el error es de validación de campos.

## 11. Pruebas funcionales con Postman

En la carpeta [`postman/`](postman/) están la colección y el entorno listos para importar:

| Archivo | Contenido |
|---|---|
| `ComercioControl_API.postman_collection.json` | 52 peticiones organizadas por módulo, con 177 aserciones automáticas |
| `ComercioControl_API.postman_environment.json` | Variable `baseUrl` y almacenamiento del token |

**Cómo usarla:**

1. Postman → *Import* → arrastrar los dos archivos.
2. Seleccionar el entorno **ComercioControl - Local** arriba a la derecha.
3. Ejecutar **`02 - Login administrador`**: el token queda guardado solo en la variable `token` y
   todas las demás peticiones lo reutilizan.
4. Botón derecho sobre la colección → **Run collection** para ejecutar la batería completa.

La colección incluye casos de **camino feliz** y casos de **error controlado**: login con contraseña
incorrecta (401), acceso con un rol sin permiso (403), producto inexistente (404), venta sin caja
abierta (409), venta con stock insuficiente (409) y producto con datos inválidos (400).

### Pruebas automáticas

Además de Postman, el proyecto trae pruebas de integración con JUnit 5 y MockMvc:

```bash
mvn test
```

## 12. Base de datos

El esquema en [`src/main/resources/schema.sql`](src/main/resources/schema.sql) es la versión SQLite del
modelo relacional entregado en la evidencia **GA6-220501096-AA2-EV01** (sentencias DDL y DML):
9 tablas, claves foráneas, restricciones `CHECK` y un índice por categoría. Los datos de prueba están
en [`data.sql`](src/main/resources/data.sql) y se cargan con `INSERT OR IGNORE`, por lo que reiniciar
la API nunca duplica registros.

```
usuarios ──< ventas >── clientes          productos >── proveedores
               │                              │
               └──< detalle_ventas >──────────┤
                                              │
usuarios ──< movimientos >────────────────────┘
usuarios ──< caja ──< ventas
usuarios ──< auditoria
```

## 13. Autor

**Iván Manuel Rodríguez David** — Ficha 3235886
Análisis y Desarrollo de Software (ADSO) — SENA
Evidencia GA7-220501096-AA5-EV04_IVO — API del proyecto

## 14. Licencia

Software propietario. © 2026 Iván Rodríguez. Todos los derechos reservados.
