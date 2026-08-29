package com.comerciocontrol.api.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calculo y verificacion del hash de contrasenas.
 *
 * <p>La API almacena las contrasenas con <b>SHA-256</b>. Se mantiene la
 * verificacion contra <b>MD5</b> unicamente por compatibilidad con las bases de
 * datos creadas por versiones anteriores de la aplicacion de escritorio, tal como
 * se advirtio en el README del proyecto.</p>
 */
@Component
public class PasswordHasher {

    /** Devuelve el hash SHA-256 en hexadecimal de la contrasena en claro. */
    public String hash(String textoPlano) {
        return digerir("SHA-256", textoPlano);
    }

    /**
     * Compara una contrasena en claro contra el hash almacenado.
     * Acepta hashes SHA-256 (64 caracteres) y MD5 heredados (32 caracteres).
     */
    public boolean coincide(String textoPlano, String hashAlmacenado) {
        if (textoPlano == null || hashAlmacenado == null) {
            return false;
        }
        String esperado = hashAlmacenado.length() == 32
                ? digerir("MD5", textoPlano)
                : digerir("SHA-256", textoPlano);
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                hashAlmacenado.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private String digerir(String algoritmo, String texto) {
        try {
            byte[] bytes = MessageDigest.getInstance(algoritmo)
                    .digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash no disponible: " + algoritmo, e);
        }
    }
}
