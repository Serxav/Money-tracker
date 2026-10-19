package com.moneytracker.users;

import java.util.Set;

/** Idiomas admitidos en la app (decisión pendiente; lista inicial). */
public final class Locales {

  public static final String DEFAULT = "es";
  public static final Set<String> SUPPORTED = Set.of("es", "ca", "en");
  public static final String PATTERN = "^(es|ca|en)$";

  private Locales() {}
}
