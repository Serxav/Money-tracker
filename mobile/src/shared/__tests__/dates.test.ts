import { isValidIsoDate, todayIso } from '../dates';

describe('dates', () => {
  it('formatea la fecha local como AAAA-MM-DD', () => {
    expect(todayIso(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('valida fechas reales', () => {
    expect(isValidIsoDate('2026-10-19')).toBe(true);
    expect(isValidIsoDate('2028-02-29')).toBe(true);
    expect(isValidIsoDate('2026-02-30')).toBe(false);
    expect(isValidIsoDate('2026-13-01')).toBe(false);
    expect(isValidIsoDate('19/10/2026')).toBe(false);
  });
});
