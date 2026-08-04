package com.peloweb.vinilos.security;

import com.peloweb.vinilos.domain.enums.RolUsuario;

import java.util.UUID;

/** Principal autenticado (se guarda en el SecurityContext desde el JwtAuthFilter). */
public record AuthUser(UUID id, String email, RolUsuario rol) {
}
