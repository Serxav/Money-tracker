import { formatMoney, MAX_AMOUNT, minorToInput, parseAmountToMinor } from '../money';

describe('parseAmountToMinor', () => {
  it('convierte a unidades menores según los decimales de la divisa', () => {
    expect(parseAmountToMinor('25,50', 2)).toBe(2550);
    expect(parseAmountToMinor('25.5', 2)).toBe(2550);
    expect(parseAmountToMinor('0,01', 2)).toBe(1);
    expect(parseAmountToMinor('1234', 0)).toBe(1234);
    expect(parseAmountToMinor('1,005', 3)).toBe(1005);
    expect(parseAmountToMinor(' 10 ', 2)).toBe(1000);
  });

  it('no pierde precisión como haría la coma flotante', () => {
    // 0.1 + 0.2 !== 0.3 en coma flotante; aquí todo es entero.
    expect(parseAmountToMinor('0,29', 2)).toBe(29);
    expect(parseAmountToMinor('19,99', 2)).toBe(1999);
  });

  it('rechaza formatos no válidos o con demasiados decimales', () => {
    expect(parseAmountToMinor('', 2)).toBeNull();
    expect(parseAmountToMinor('abc', 2)).toBeNull();
    expect(parseAmountToMinor('-5', 2)).toBeNull();
    expect(parseAmountToMinor('1,234', 2)).toBeNull();
    expect(parseAmountToMinor('12,5', 0)).toBeNull();
    expect(parseAmountToMinor('1.000,50', 2)).toBeNull();
  });

  it('rechaza importes por encima del máximo de la API', () => {
    expect(parseAmountToMinor(String(MAX_AMOUNT / 100), 2)).toBe(MAX_AMOUNT);
    expect(parseAmountToMinor(String(MAX_AMOUNT / 100 + 1), 2)).toBeNull();
  });
});

describe('minorToInput', () => {
  it('es el inverso de parseAmountToMinor', () => {
    expect(minorToInput(2550, 2)).toBe('25,50');
    expect(minorToInput(5, 2)).toBe('0,05');
    expect(minorToInput(1234, 0)).toBe('1234');
    expect(minorToInput(1005, 3)).toBe('1,005');
    expect(minorToInput(-250, 2)).toBe('-2,50');
    expect(parseAmountToMinor(minorToInput(98765, 2), 2)).toBe(98765);
  });
});

describe('formatMoney', () => {
  it('muestra los decimales de cada divisa', () => {
    expect(formatMoney(2550, 'EUR', 2)).toMatch(/25,50/);
    expect(formatMoney(1234, 'JPY', 0, 'es-ES')).toMatch(/1234|1\.234/);
    expect(formatMoney(1005, 'KWD', 3)).toMatch(/1,005/);
    expect(formatMoney(-2550, 'EUR', 2)).toMatch(/-25,50/);
  });
});
