import { accountSchema, categorySchema, movementSchema } from '../schemas';

/** Respuestas reales del backend 0.1 (contrato backend/openapi.yaml). */
const movement = {
  id: 'f88dd74f-f315-4947-bbec-f5a772b9531a',
  accountId: '2db5e501-a26d-4dde-9902-b3060bfd0b8c',
  categoryId: 'b07110df-bf40-4994-9f71-f9b344e7d62f',
  type: 'EXPENSE',
  amount: 2550,
  currency: 'EUR',
  date: '2026-10-20',
  description: 'Supermercado',
  createdAt: '2026-10-19T22:11:04.051459Z',
  updatedAt: '2026-10-19T22:11:04.051459Z',
};

it('acepta un movimiento tal como lo devuelve el backend', () => {
  const result = movementSchema.safeParse(movement);
  expect(result.error?.issues).toBeUndefined();
});

it('acepta un movimiento sin descripción (el backend omite los nulos o envía null)', () => {
  const { description: _omit, ...withoutDescription } = movement;
  expect(movementSchema.safeParse(withoutDescription).success).toBe(true);
  expect(movementSchema.safeParse({ ...movement, description: null }).success).toBe(true);
});

it('acepta cuentas y categorías reales', () => {
  expect(
    accountSchema.safeParse({
      id: '2db5e501-a26d-4dde-9902-b3060bfd0b8c',
      name: 'Banco',
      currency: 'EUR',
      fractionDigits: 2,
      initialBalance: 10000,
      balance: 7450,
      createdAt: '2026-10-19T22:10:00.1Z',
    }).success,
  ).toBe(true);
  expect(
    categorySchema.safeParse({
      id: 'b07110df-bf40-4994-9f71-f9b344e7d62f',
      name: 'Comida',
      type: 'EXPENSE',
      icon: 'food',
      color: '#E07A5F',
    }).success,
  ).toBe(true);
});
