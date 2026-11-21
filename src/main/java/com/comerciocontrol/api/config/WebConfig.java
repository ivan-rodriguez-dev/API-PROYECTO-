package com.comerciocontrol.api.config;

import com.comerciocontrol.api.security.AutenticacionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el interceptor de seguridad y habilita CORS para que el modulo
 * frontend pueda consumir la API desde otro origen. Solo se aceptan los
 * origenes configurados en {@code app.cors.origenes}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Rutas publicas: no exigen token. */
    private static final String[] RUTAS_PUBLICAS = {
            "/api/auth/login",
            "/api/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error"
    };

    private final AutenticacionInterceptor autenticacionInterceptor;
    private final String[] origenesPermitidos;

    public WebConfig(AutenticacionInterceptor autenticacionInterceptor,
                     @Value("${app.cors.origenes}") String[] origenesPermitidos) {
        this.autenticacionInterceptor = autenticacionInterceptor;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(RUTAS_PUBLICAS);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
