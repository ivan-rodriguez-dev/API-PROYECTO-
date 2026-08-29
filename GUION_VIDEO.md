# Guion del video de sustentación — 3 minutos

**Evidencia:** GA7-220501096-AA5-EV04_IVO — API del proyecto
**Aprendiz:** Iván Manuel Rodríguez David · Ficha 3235886

---

## Antes de grabar (5 minutos de preparación)

1. Abrir una terminal en la carpeta del proyecto y arrancar la API:

```bash
mvn spring-boot:run
```

2. Esperar a que aparezca `Started ApiApplication`.
3. Abrir **Postman** e importar los dos archivos de la carpeta `postman/`.
4. Seleccionar el entorno **ComercioControl - Local** (arriba a la derecha).
5. Abrir estas pestañas en el navegador, en este orden:
   - `http://localhost:8080/swagger-ui.html`
   - El repositorio en GitHub (o la carpeta local si aún no lo has subido)
6. Tener el `README.md` abierto en el editor.
7. Grabar con OBS o con la Grabadora de Xbox (`Win + G`). Voz clara, sin música.

> **Importante:** si ya ejecutaste la colección antes, borra el archivo `comerciocontrol.db`
> y reinicia la API para que la caja empiece cerrada y la demostración salga igual que aquí.

---

## Guion (con cronómetro)

### 🎬 0:00 – 0:25 — Presentación y descripción general

> «Buenos días. Soy Iván Manuel Rodríguez David, ficha 3235886, y presento la evidencia
> GA7-AA5-EV04: la API de mi proyecto formativo **ComercioControl**, un sistema de punto de
> venta e inventario para comercio minorista.
>
> Hasta ahora ComercioControl era una aplicación de escritorio en JavaFX, donde la interfaz y
> la base de datos vivían en el mismo equipo. En el plan de desarrollo definí que la fase dos
> del proyecto es una **aplicación móvil en Flutter**, y una app móvil no puede abrir el archivo
> de base de datos del computador de la tienda. Esa es la razón de esta API: expone por HTTP los
> mismos procesos de negocio para que los consuma cualquier cliente. El proyecto pasa de una
> arquitectura monolítica de escritorio a una arquitectura **cliente-servidor**.»

*(En pantalla: el README abierto en el diagrama de la sección 1.)*

### 🎬 0:25 – 0:55 — Relación con los requerimientos y endpoints

> «La API está construida en **Java 17 con Spring Boot 3** y persiste sobre **SQLite**, las mismas
> tecnologías que ratifiqué en el plan de desarrollo.
>
> Cada requerimiento funcional del proyecto tiene su servicio web. En esta tabla del README se ve
> la trazabilidad: el requerimiento de autenticación es `POST /api/auth/login`; la gestión de
> inventario es el CRUD de `/api/productos`; el registro de ventas del POS es `POST /api/ventas`;
> y así con clientes, proveedores, movimientos de stock, caja, reportes y auditoría.
>
> Son **38 endpoints** organizados en nueve módulos. Toda la documentación se genera sola con
> OpenAPI: aquí en **Swagger** se ve cada operación, sus parámetros, los códigos de respuesta y
> se puede probar desde el navegador.»

*(En pantalla: tabla de trazabilidad del README → cambiar a Swagger UI y desplegar la etiqueta
"05 - Ventas (POS)".)*

### 🎬 0:55 – 2:05 — Demostración de pruebas funcionales en Postman

> «Ahora las pruebas funcionales en **Postman**. La colección trae 25 peticiones organizadas en el
> orden real del proceso de negocio, con pruebas automáticas en cada una.
>
> Empiezo con el **login**: envío usuario y contraseña, la API responde 200 con un **token JWT** y
> el script lo guarda automáticamente para las demás peticiones.
>
> Con ese token consulto el inventario. Fíjense en que cada producto trae dos campos calculados:
> el **estado del stock** y la **utilidad unitaria**.
>
> Y ahora el caso más importante, el **flujo del punto de venta**. Primero intento registrar una
> venta sin caja abierta: la API responde **409** y explica que hay que abrir la caja. Esa es la
> primera regla de negocio del proyecto.
>
> Abro la caja con una base de cien mil pesos: **201 Created**.
>
> Ahora sí registro la venta: tres unidades de arroz y dos de leche. Observen que yo solo envío el
> **id del producto y la cantidad**: los precios los toma el servidor de la base de datos, y calcula
> subtotal diecisiete mil trescientos, descuento trescientos, **IVA del 19 %** tres mil doscientos
> treinta, y total veinte mil doscientos treinta. El cliente nunca decide los importes.
>
> Compruebo la integración con la base de datos: el stock del arroz pasó de 120 a **117**, y la venta
> dejó su **movimiento de salida** en el kardex.
>
> Ahora la validación de stock: intento vender nueve mil unidades de aceite, del que solo hay
> cuatro. La API responde **409** diciendo cuánto hay disponible, y como la venta es una transacción
> atómica, **no se modificó nada**.
>
> Cierro la caja: calcula las ventas del día y la diferencia de menos doscientos treinta pesos, o
> sea el faltante.
>
> Y en control de acceso: con el token del **vendedor** intento crear un producto y recibo **403**,
> porque el inventario es del administrador y del bodeguero.»

*(En pantalla: ejecutar las peticiones de la carpeta "05 - Flujo completo del punto de venta" una
por una, mostrando el cuerpo de la respuesta y la pestaña **Test Results** en verde.)*

### 🎬 2:05 – 2:25 — Resultados obtenidos

> «Ejecuto la colección completa con **Run collection**… y aquí está el resultado: **todas las
> pruebas en verde**, incluyendo los casos de error controlado: 400 por datos inválidos, 401 sin
> token, 403 por rol sin permiso, 404 por recurso inexistente y 409 por regla de negocio.
>
> Además el proyecto tiene **30 pruebas automáticas** con JUnit y MockMvc que se ejecutan con
> `mvn test`, y todas pasan.»

*(En pantalla: el Runner de Postman con el resumen en verde → cambiar a la terminal con el
`Tests run: 30, Failures: 0, Errors: 0` y el `BUILD SUCCESS`.)*

### 🎬 2:25 – 2:50 — Repositorio y README

> «El código está versionado con **Git**. Esta es la URL del repositorio.
>
> La estructura sigue una **arquitectura en capas**: `controller` recibe las peticiones,
> `service` aplica las reglas de negocio y las transacciones, y `repository` es la única capa que
> ejecuta SQL, siempre con consultas parametrizadas. Es la misma separación del proyecto de
> escritorio, trasladada al mundo web.
>
> El historial tiene **catorce commits** con mensajes descriptivos, uno por cada módulo construido:
> el proyecto Maven, el esquema de base de datos, la seguridad, el POS, los endpoints, las pruebas.
>
> Y el **README** documenta todo: la justificación de la API, la tabla de trazabilidad con los
> requerimientos, el stack tecnológico, las diez reglas de negocio implementadas, la matriz de
> permisos por rol, la tabla de códigos de error, cómo ejecutar el proyecto y el catálogo completo
> de los 38 endpoints.»

*(En pantalla: GitHub o `git log --oneline` en la terminal → recorrer el README rápidamente.)*

### 🎬 2:50 – 3:00 — Cierre

> «En resumen: los requerimientos del proyecto quedaron convertidos en servicios web funcionales,
> con lógica de negocio, persistencia, validaciones, manejo de errores, documentación y pruebas.
> Muchas gracias.»

---

## Comandos de apoyo (para tenerlos copiados)

Mostrar el historial de commits:

```bash
git log --oneline
```

Ejecutar las pruebas automáticas:

```bash
mvn test
```

Arrancar la API:

```bash
mvn spring-boot:run
```

---

## Lista de verificación antes de subir el video

- [ ] Duración **máxima 3 minutos**.
- [ ] Se ve el rostro o al menos se escucha la voz del aprendiz.
- [ ] Se muestra la URL del repositorio de forma legible.
- [ ] Se ve el `git log` con los commits.
- [ ] Se ve al menos una respuesta JSON exitosa y una de error.
- [ ] Se ven las pruebas de Postman en verde.
- [ ] Se recorre el README.
- [ ] Subido a YouTube (no listado), Drive o OneDrive con **enlace de acceso público**.
- [ ] Enlace pegado en el espacio GA7-220501096-AA5-EV04_IVO de la plataforma.
