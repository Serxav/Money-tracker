package com.moneytracker.currencies;

/** Divisa soportada: código ISO 4217 y número de decimales de su unidad menor. */
public record CurrencyResponse(String code, int fractionDigits, String name) {}
