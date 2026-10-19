package com.moneytracker.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Confirmación con contraseña para borrar la cuenta y todos sus datos. */
public record DeleteMeRequest(@NotBlank @Size(max = 128) String password) {}
