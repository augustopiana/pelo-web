package com.peloweb.vinilos.security;

import com.peloweb.vinilos.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Emision y validacion de JWT (HS256). Tres tipos de token:
 * - access  (corto): autentica las requests.
 * - refresh (largo): renueva el access.
 * - verify  (medio): verifica el email (link enviado por mail).
 */
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_VERIFY = "verify";

    private static final String CLAIM_TYPE = "typ";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROL = "rol";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Duration verifyTtl;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl}") Duration accessTtl,
            @Value("${app.jwt.refresh-ttl}") Duration refreshTtl,
            @Value("${app.jwt.verify-ttl}") Duration verifyTtl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.verifyTtl = verifyTtl;
    }

    public String generateAccess(Usuario u) {
        return build(u, TYPE_ACCESS, accessTtl, true);
    }

    public String generateRefresh(Usuario u) {
        return build(u, TYPE_REFRESH, refreshTtl, false);
    }

    public String generateVerification(Usuario u) {
        return build(u, TYPE_VERIFY, verifyTtl, false);
    }

    private String build(Usuario u, String type, Duration ttl, boolean includeUserClaims) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(u.getId().toString())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key);
        if (includeUserClaims) {
            builder.claim(CLAIM_EMAIL, u.getEmail());
            builder.claim(CLAIM_ROL, u.getRol().name());
        }
        return builder.compact();
    }

    /** Valida firma y expiracion; lanza JwtException si el token es invalido/vencido. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public UUID userId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String type(Claims claims) {
        return claims.get(CLAIM_TYPE, String.class);
    }

    public String email(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public String rol(Claims claims) {
        return claims.get(CLAIM_ROL, String.class);
    }
}
