package com.moneytracker.users;

import jakarta.validation.constraints.Pattern;

/** Campos editables del usuario; los nulos no se modifican. */
public record UpdateMeRequest(
    @Pattern(regexp = "^[A-Z]{3}$", message = "Código ISO 4217 de 3 letras") String baseCurrency,
    @Pattern(regexp = Locales.PATTERN, message = "Idioma no soportado") String locale) {}
