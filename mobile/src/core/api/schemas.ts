import { z } from 'zod';

/** Esquemas de las respuestas de la API (contrato: backend/openapi.yaml). */

export const movementTypeSchema = z.enum(['INCOME', 'EXPENSE']);
export type MovementType = z.infer<typeof movementTypeSchema>;

export const userSchema = z.object({
  id: z.string().uuid(),
  email: z.string(),
  baseCurrency: z.string().length(3),
  locale: z.string(),
  createdAt: z.string(),
});
export type User = z.infer<typeof userSchema>;

export const authResponseSchema = z.object({
  accessToken: z.string().min(1),
  tokenType: z.literal('Bearer'),
  expiresIn: z.number().int().positive(),
  refreshToken: z.string().min(1),
  user: userSchema,
});
export type AuthResponse = z.infer<typeof authResponseSchema>;

export const currencySchema = z.object({
  code: z.string().length(3),
  fractionDigits: z.number().int().min(0).max(4),
  name: z.string(),
});
export type Currency = z.infer<typeof currencySchema>;

export const accountSchema = z.object({
  id: z.string().uuid(),
  name: z.string(),
  currency: z.string().length(3),
  fractionDigits: z.number().int().min(0).max(4),
  initialBalance: z.number().int(),
  balance: z.number().int(),
  createdAt: z.string(),
});
export type Account = z.infer<typeof accountSchema>;

export const categorySchema = z.object({
  id: z.string().uuid(),
  name: z.string(),
  type: movementTypeSchema,
  icon: z.string().nullish(),
  color: z.string().nullish(),
});
export type Category = z.infer<typeof categorySchema>;

export const movementSchema = z.object({
  id: z.string().uuid(),
  accountId: z.string().uuid(),
  categoryId: z.string().uuid(),
  type: movementTypeSchema,
  amount: z.number().int().positive(),
  currency: z.string().length(3),
  date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
  description: z.string().nullish(),
  createdAt: z.string(),
  updatedAt: z.string(),
});
export type Movement = z.infer<typeof movementSchema>;

export function pageSchema<T extends z.ZodTypeAny>(item: T) {
  return z.object({
    items: z.array(item),
    page: z.number().int().min(0),
    limit: z.number().int().min(1),
    total: z.number().int().min(0),
  });
}
export type Page<T> = { items: T[]; page: number; limit: number; total: number };

export const apiErrorBodySchema = z.object({
  code: z.string(),
  message: z.string(),
  requestId: z.string().optional(),
  details: z.array(z.object({ field: z.string(), message: z.string() })).optional(),
});
