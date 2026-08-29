package com.comerciocontrol.api.config;

import com.comerciocontrol.api.security.AutenticacionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el interceptor de seguridad y habilita CORS para que el modulo
 * frontend (HTML/CSS/JS de la evidencia GA6) y la futura app movil puedan
 * consumir la API desde otro origen.
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

    public WebConfig(AutenticacionInterceptor autenticacionInterceptor) {
        this.autenticacionInterceptor = autenticacionInterceptor;
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
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
