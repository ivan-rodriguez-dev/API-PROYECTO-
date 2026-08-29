package com.comerciocontrol.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Generacion y validacion de los tokens JWT que autentican las peticiones. */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMillis;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMillis = expiracionMinutos * 60_000L;
    }

    /** Emite un token firmado con los datos del usuario que inicio sesion. */
    public String generar(UsuarioAutenticado usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.usuario())
                .claim("id", usuario.id())
                .claim("nombre", usuario.nombre())
                .claim("rol", usuario.rol())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMillis))
                .signWith(clave)
                .compact();
    }

    /**
     * Valida la firma y la vigencia del token y reconstruye el usuario.
     * @return el usuario autenticado, o {@code null} si el token es invalido o expiro.
     */
    public UsuarioAutenticado validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload();
            return new UsuarioAutenticado(
                    c.get("id", Integer.class),
                    c.getSubject(),
                    c.get("nombre", String.class),
                    c.get("rol", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** Minutos de vigencia del token, para informarlo en la respuesta del login. */
    public long getExpiracionSegundos() {
        return expiracionMillis / 1000;
    }
}
