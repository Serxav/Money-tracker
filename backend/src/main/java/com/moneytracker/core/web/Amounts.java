package com.moneytracker.core.web;

/** Límites de importes en unidades menores (céntimos, yenes, fils...). */
public final class Amounts {

  /** 10^15 unidades menores: holgado para cualquier uso personal y sin riesgo de desbordar. */
  public static final long MAX = 1_000_000_000_000_000L;

  private Amounts() {}
}
