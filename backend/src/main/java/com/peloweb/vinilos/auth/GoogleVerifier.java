package com.peloweb.vinilos.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.peloweb.vinilos.web.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Verifica el ID token de Google (login con Google).
 * Scaffold: si no hay app.google.client-id configurado, queda deshabilitado.
 */
@Component
public class GoogleVerifier {

    private final String clientId;

    public GoogleVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    public boolean isEnabled() {
        return clientId != null && !clientId.isBlank();
    }

    public GooglePayload verify(String idTokenString) {
        if (!isEnabled()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Login con Google no configurado");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "ID token de Google invalido");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            return new GooglePayload(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    Boolean.TRUE.equals(payload.getEmailVerified()));
        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No se pudo verificar el token de Google");
        }
    }

    public record GooglePayload(String sub, String email, String nombre, boolean emailVerificado) {
    }
}
