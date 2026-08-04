package com.peloweb.vinilos.auth.dto;

import com.peloweb.vinilos.domain.Usuario;

import java.util.UUID;

public record UsuarioDTO(
        UUID id,
        String nombre,
        String email,
        String rol,
        boolean emailVerificado) {

    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol().name(), u.isEmailVerificado());
    }
}
