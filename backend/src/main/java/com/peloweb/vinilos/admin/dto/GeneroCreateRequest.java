package com.peloweb.vinilos.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record GeneroCreateRequest(@NotBlank String nombre) {
}
