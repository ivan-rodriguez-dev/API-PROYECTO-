package com.comerciocontrol.api.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calculo y verificacion del hash de contrasenas.
 *
 * <p>La API almacena las contrasenas nuevas con <b>BCrypt</b>. Se mantiene la
 * verificacion contra <b>SHA-256</b> y <b>MD5</b> unicamente por compatibilidad con
 * los datos de desarrollo y con las bases de datos creadas por versiones anteriores
 * de la aplicacion de escritorio, tal como se advirtio en el README del proyecto.</p>
 */
@Component
public class PasswordHasher {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    /** Devuelve el hash BCrypt de la contrasena en claro. */
    public String hash(String textoPlano) {
        return bcrypt.encode(textoPlano);
    }

    /**
     * Compara una contrasena en claro contra el hash almacenado.
     * Acepta BCrypt y los hashes heredados SHA-256 (64 caracteres) y MD5 (32 caracteres).
     */
    public boolean coincide(String textoPlano, String hashAlmacenado) {
        if (textoPlano == null || hashAlmacenado == null) {
            return false;
        }
        if (hashAlmacenado.startsWith("$2")) {
            return bcrypt.matches(textoPlano, hashAlmacenado);
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
