package com.comerciocontrol.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pagina de bienvenida de la API.
 *
 * <p>Al ser un servicio REST, la raiz no serviria ningun contenido y el navegador
 * mostraria un error. Este indice orienta a quien abre {@code http://localhost:8080}
 * hacia la documentacion y hacia la comprobacion de estado.</p>
 */
@RestController
@Tag(name = "00 - Estado", description = "Comprobacion de disponibilidad de la API")
public class IndexController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @SecurityRequirements
    @Operation(summary = "Indice de la API",
            description = "Pagina de bienvenida con los enlaces a la documentacion y al estado del servicio.")
    public String indice() {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>ComercioControl API</title>
                  <style>
                    :root { color-scheme: light dark; }
                    body { font-family: system-ui, "Segoe UI", sans-serif; line-height: 1.6;
                           max-width: 720px; margin: 0 auto; padding: 2.5rem 1.25rem; }
                    h1 { margin-bottom: .25rem; }
                    .sub { color: #6b7280; margin-top: 0; }
                    .ok { display: inline-block; background: #dcfce7; color: #166534;
                          border-radius: 999px; padding: .15rem .7rem; font-size: .85rem;
                          font-weight: 600; }
                    ul { padding-left: 1.1rem; }
                    li { margin: .45rem 0; }
                    code { background: rgba(127,127,127,.18); padding: .1rem .35rem;
                           border-radius: 4px; font-size: .92em; }
                    footer { margin-top: 2.5rem; border-top: 1px solid rgba(127,127,127,.3);
                             padding-top: 1rem; color: #6b7280; font-size: .88rem; }
                  </style>
                </head>
                <body>
                  <h1>ComercioControl API</h1>
                  <p class="sub">Punto de venta e inventario &middot; version 1.0.0 &nbsp; <span class="ok">EN EJECUCION</span></p>

                  <h2>Empezar aqui</h2>
                  <ul>
                    <li><a href="/swagger-ui.html"><strong>Documentacion interactiva (Swagger UI)</strong></a>
                        &mdash; los 38 endpoints, con sus parametros y sus codigos de respuesta</li>
                    <li><a href="/api/health">Estado del servicio y de la base de datos</a></li>
                    <li><a href="/v3/api-docs">Especificacion OpenAPI 3 en JSON</a></li>
                  </ul>

                  <h2>Autenticacion</h2>
                  <p>Todos los recursos, salvo esta pagina, <code>/api/health</code> y
                     <code>/api/auth/login</code>, exigen un token JWT en la cabecera
                     <code>Authorization: Bearer &lt;token&gt;</code>.</p>
                  <p>Para obtenerlo: <code>POST /api/auth/login</code> con
                     <code>{"usuario": "ivan.admin", "password": "Admin2026*"}</code></p>
                  <p>Por eso abrir <code>/api/productos</code> directamente en el navegador
                     responde <code>401</code>: el navegador no envia el token. Use Swagger UI
                     o Postman.</p>

                  <footer>
                    Evidencia GA7-220501096-AA5-EV04_IVO &mdash; API del proyecto<br>
                    Ivan Manuel Rodriguez David &middot; Ficha 3235886 &middot; ADSO &mdash; SENA
                  </footer>
                </body>
                </html>
                """;
    }
}
