package com.comerciocontrol.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuracion de la documentacion OpenAPI 3 (Swagger UI).
 * Disponible en http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI comercioControlOpenAPI() {
        final String esquemaJwt = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("ComercioControl API")
                        .version("1.0.0")
                        .description("""
                                API REST del proyecto formativo **ComercioControl** \
                                (punto de venta e inventario para comercio minorista).

                                Expone los procesos de negocio del sistema para que puedan ser \
                                consumidos por la aplicacion de escritorio JavaFX, por la aplicacion \
                                movil Flutter prevista en la fase 2 y por cualquier cliente HTTP.

                                **Autenticacion:** todos los recursos, salvo `/api/auth/login` y \
                                `/api/health`, exigen un token JWT en la cabecera \
                                `Authorization: Bearer <token>`.

                                **Usuarios de prueba**
                                | Usuario | Contrasena | Rol |
                                |---|---|---|
                                | ivan.admin | Admin2026* | administrador |
                                | laura.ventas | Venta2026* | vendedor |
                                | carlos.bodega | Bodega2026* | bodeguero |

                                Evidencia GA7-220501096-AA5-EV04 - Ivan Manuel Rodriguez David - Ficha 3235886
                                """)
                        .contact(new Contact()
                                .name("Ivan Manuel Rodriguez David")
                                .email("ivan13gamer@gmail.com")
                                .url("https://github.com/JuanMa3132/Comercio-ControlV1"))
                        .license(new License().name("Software propietario - (c) 2026 Ivan Rodriguez")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Entorno local de desarrollo")))
                .addSecurityItem(new SecurityRequirement().addList(esquemaJwt))
                .components(new Components().addSecuritySchemes(esquemaJwt,
                        new SecurityScheme()
                                .name(esquemaJwt)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token obtenido en POST /api/auth/login")));
    }
}
