/**
 * Importes en unidades menores (enteros), como en la API. Nunca se usa coma flotante para
 * calcular: solo para mostrar con Intl, y dentro del rango seguro de Number.
 */

export const MAX_AMOUNT = 1_000_000_000_000_000;

/** Convierte "25,50" o "25.5" en 2550 (EUR) — devuelve null si no es válido. */
export function parseAmountToMinor(input: string, fractionDigits: number): number | null {
  const normalized = input.trim().replace(/\s/g, '');
  if (normalized.length === 0) {
    return null;
  }
  const match = /^(\d+)(?:[.,](\d*))?$/.exec(normalized);
  if (!match) {
    return null;
  }
  const [, whole, decimals = ''] = match;
  if (decimals.length > fractionDigits) {
    return null;
  }
  const digits = whole + decimals.padEnd(fractionDigits, '0');
  const value = Number(digits.replace(/^0+(?=\d)/, ''));
  if (!Number.isSafeInteger(value) || value > MAX_AMOUNT) {
    return null;
  }
  return value;
}

/** Texto editable a partir de unidades menores: 2550 → "25,50" (EUR), 1234 → "1234" (JPY). */
export function minorToInput(amount: number, fractionDigits: number, decimalSeparator = ','): string {
  const negative = amount < 0;
  const digits = Math.abs(amount).toString().padStart(fractionDigits + 1, '0');
  const whole = fractionDigits === 0 ? digits : digits.slice(0, -fractionDigits);
  const decimals = fractionDigits === 0 ? '' : digits.slice(-fractionDigits);
  return `${negative ? '-' : ''}${whole}${decimals ? decimalSeparator + decimals : ''}`;
}

/** Formato para mostrar con el símbolo de la divisa: 2550, EUR → "25,50 €". */
export function formatMoney(
  amount: number,
  currency: string,
  fractionDigits: number,
  locale = 'es-ES',
): string {
  const major = Number(minorToInput(amount, fractionDigits, '.'));
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(major);
}
