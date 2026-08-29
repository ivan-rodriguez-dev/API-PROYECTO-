package com.comerciocontrol.api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe un endpoint a los roles indicados.
 * Si no se anota, basta con estar autenticado.
 *
 * <p>Ejemplo: {@code @RolesPermitidos({"administrador", "bodeguero"})}</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RolesPermitidos {
    String[] value();
}
