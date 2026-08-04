package com.peloweb.vinilos.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UsuarioDTO usuario) {
}
