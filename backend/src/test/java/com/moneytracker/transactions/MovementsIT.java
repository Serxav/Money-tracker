package com.moneytracker.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.moneytracker.support.ApiClient.Response;
import com.moneytracker.support.IntegrationTest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MovementsIT extends IntegrationTest {

  private record Fixture(Session s, String account, String expenseCat, String incomeCat) {}

  private Fixture fixture(String currency) {
    Session s = register();
    String account =
        api.post("/v1/accounts", "{\"name\":\"Cuenta\",\"currency\":\"" + currency + "\",\"initialBalance\":10000}", s.accessToken())
            .json("$.id");
    String expense = api.get("/v1/categories?type=EXPENSE", s.accessToken()).json("$.items[0].id");
    String income = api.get("/v1/categories?type=INCOME", s.accessToken()).json("$.items[0].id");
    return new Fixture(s, account, expense, income);
  }

  private static String body(String account, String category, String type, long amount, String date, String desc) {
    return "{\"accountId\":\"" + account + "\",\"categoryId\":\"" + category + "\",\"type\":\"" + type
        + "\",\"amount\":" + amount + ",\"date\":\"" + date + "\""
        + (desc == null ? "" : ",\"description\":\"" + desc + "\"") + "}";
  }

  private Response create(Fixture f, String type, long amount, String date) {
    String cat = type.equals("INCOME") ? f.incomeCat() : f.expenseCat();
    return api.post("/v1/transactions", body(f.account(), cat, type, amount, date, null), f.s().accessToken());
  }

  @Test
  void createdMovementsInheritAccountCurrencyAndUpdateBalance() {
    Fixture f = fixture("EUR");
    Response expense =
        api.post("/v1/transactions", body(f.account(), f.expenseCat(), "EXPENSE", 2550, "2026-10-01", "  Súper  "), f.s().accessToken());
    assertThat(expense.status()).as(expense.body()).isEqualTo(201);
    assertThat((String) expense.json("$.currency")).isEqualTo("EUR");
    assertThat((String) expense.json("$.description")).isEqualTo("Súper");
    assertThat((Integer) expense.json("$.amount")).isEqualTo(2550);
    assertThat((String) expense.json("$.date")).isEqualTo("2026-10-01");
    assertThat(create(f, "INCOME", 100_000, "2026-10-02").status()).isEqualTo(201);

    Response account = api.get("/v1/accounts/" + f.account(), f.s().accessToken());
    assertThat(((Number) account.json("$.balance")).longValue()).isEqualTo(10_000 - 2_550 + 100_000);
  }

  @Test
  void currenciesWithZeroAndThreeDecimalsKeepExactAmounts() {
    Fixture yen = fixture("JPY");
    assertThat(create(yen, "EXPENSE", 1234, "2026-10-01").status()).isEqualTo(201);
    assertThat(((Number) api.get("/v1/accounts/" + yen.account(), yen.s().accessToken()).json("$.balance")).longValue())
        .isEqualTo(10_000 - 1_234);
    Fixture dinar = fixture("KWD");
    assertThat(create(dinar, "INCOME", 1_005, "2026-10-01").status()).isEqualTo(201);
    Response acc = api.get("/v1/accounts/" + dinar.account(), dinar.s().accessToken());
    assertThat((Integer) acc.json("$.fractionDigits")).isEqualTo(3);
    assertThat(((Number) acc.json("$.balance")).longValue()).isEqualTo(11_005);
  }

  @Test
  void invalidAmountsAndDatesAreRejected() {
    Fixture f = fixture("EUR");
    assertThat(create(f, "EXPENSE", 0, "2026-10-01").status()).isEqualTo(400);
    assertThat(create(f, "EXPENSE", -5, "2026-10-01").status()).isEqualTo(400);
    assertThat(create(f, "EXPENSE", 1_000_000_000_000_001L, "2026-10-01").status()).isEqualTo(400);
    assertThat(create(f, "EXPENSE", 100, "2026-13-40").status()).isEqualTo(400);
    Response tooFar = create(f, "EXPENSE", 100, LocalDate.now().plusYears(2).toString());
    assertThat(tooFar.status()).isEqualTo(422);
    assertThat((String) tooFar.json("$.code")).isEqualTo("INVALID_DATE");
    Response floatAmount =
        api.post("/v1/transactions", body(f.account(), f.expenseCat(), "EXPENSE", 1, "2026-10-01", null).replace("\"amount\":1", "\"amount\":12.5"), f.s().accessToken());
    assertThat(floatAmount.status()).isEqualTo(400);
    Response stringAmount =
        api.post("/v1/transactions", body(f.account(), f.expenseCat(), "EXPENSE", 1, "2026-10-01", null).replace("\"amount\":1", "\"amount\":\"1\""), f.s().accessToken());
    assertThat(stringAmount.status()).isEqualTo(400);
  }

  @Test
  void categoryMustMatchMovementType() {
    Fixture f = fixture("EUR");
    Response res =
        api.post("/v1/transactions", body(f.account(), f.incomeCat(), "EXPENSE", 100, "2026-10-01", null), f.s().accessToken());
    assertThat(res.status()).isEqualTo(422);
    assertThat((String) res.json("$.code")).isEqualTo("CATEGORY_TYPE_MISMATCH");
  }

  @Test
  void cannotUseOrSeeOtherUsersAccountsCategoriesOrMovements() {
    Fixture owner = fixture("EUR");
    Fixture attacker = fixture("EUR");
    String movement = create(owner, "EXPENSE", 500, "2026-10-01").json("$.id");

    // Usar la cuenta o categoría de otro usuario.
    assertThat(api.post("/v1/transactions", body(owner.account(), attacker.expenseCat(), "EXPENSE", 1, "2026-10-01", null), attacker.s().accessToken()).status())
        .isEqualTo(404);
    assertThat(api.post("/v1/transactions", body(attacker.account(), owner.expenseCat(), "EXPENSE", 1, "2026-10-01", null), attacker.s().accessToken()).status())
        .isEqualTo(404);
    // Leer, modificar o borrar el movimiento de otro usuario.
    String token = attacker.s().accessToken();
    assertThat(api.get("/v1/transactions/" + movement, token).status()).isEqualTo(404);
    assertThat(api.put("/v1/transactions/" + movement, body(attacker.account(), attacker.expenseCat(), "EXPENSE", 1, "2026-10-01", null), token).status())
        .isEqualTo(404);
    assertThat(api.delete("/v1/transactions/" + movement, null, token).status()).isEqualTo(404);
    assertThat((Integer) api.get("/v1/transactions?accountId=" + owner.account(), token).json("$.total")).isZero();
    assertThat((Integer) api.get("/v1/transactions", owner.s().accessToken()).json("$.total")).isEqualTo(1);
  }

  @Test
  void listIsFilteredSortedAndPaginated() {
    Fixture f = fixture("EUR");
    create(f, "EXPENSE", 100, "2026-09-15");
    create(f, "INCOME", 200, "2026-10-01");
    create(f, "EXPENSE", 300, "2026-10-20");
    String token = f.s().accessToken();

    Response all = api.get("/v1/transactions", token);
    List<String> dates = all.json("$.items[*].date");
    assertThat(dates).containsExactly("2026-10-20", "2026-10-01", "2026-09-15");

    assertThat((Integer) api.get("/v1/transactions?type=EXPENSE", token).json("$.total")).isEqualTo(2);
    assertThat((Integer) api.get("/v1/transactions?from=2026-10-01&to=2026-10-31", token).json("$.total")).isEqualTo(2);
    assertThat((Integer) api.get("/v1/transactions?categoryId=" + f.incomeCat(), token).json("$.total")).isEqualTo(1);

    Response page = api.get("/v1/transactions?limit=2&page=1", token);
    assertThat((Integer) page.json("$.total")).isEqualTo(3);
    assertThat((List<?>) page.json("$.items")).hasSize(1);

    Response badRange = api.get("/v1/transactions?from=2026-10-31&to=2026-10-01", token);
    assertThat((String) badRange.json("$.code")).isEqualTo("INVALID_DATE_RANGE");
  }

  @Test
  void idempotencyKeyPreventsDuplicates() {
    Fixture f = fixture("EUR");
    String json = body(f.account(), f.expenseCat(), "EXPENSE", 999, "2026-10-01", "Taxi");
    Map<String, String> headers = Map.of("Idempotency-Key", "retry-key-0001");

    Response first = api.send("POST", "/v1/transactions", json, f.s().accessToken(), headers);
    Response retry = api.send("POST", "/v1/transactions", json, f.s().accessToken(), headers);
    assertThat(first.status()).isEqualTo(201);
    assertThat(retry.status()).isEqualTo(200);
    assertThat(retry.header("Idempotent-Replayed")).isEqualTo("true");
    assertThat((String) retry.json("$.id")).isEqualTo(first.json("$.id"));
    assertThat((Integer) api.get("/v1/transactions", f.s().accessToken()).json("$.total")).isEqualTo(1);

    Response reused =
        api.send("POST", "/v1/transactions", json.replace("999", "1000"), f.s().accessToken(), headers);
    assertThat(reused.status()).isEqualTo(422);
    assertThat((String) reused.json("$.code")).isEqualTo("IDEMPOTENCY_KEY_REUSED");

    Response badKey =
        api.send("POST", "/v1/transactions", json, f.s().accessToken(), Map.of("Idempotency-Key", "x"));
    assertThat(badKey.status()).isEqualTo(400);

    // La misma clave de otro usuario no interfiere.
    Fixture other = fixture("EUR");
    String otherJson = body(other.account(), other.expenseCat(), "EXPENSE", 999, "2026-10-01", "Taxi");
    assertThat(api.send("POST", "/v1/transactions", otherJson, other.s().accessToken(), headers).status())
        .isEqualTo(201);
  }

  @Test
  void movementCanBeUpdatedAndMovedToAnotherAccountCurrency() {
    Fixture f = fixture("EUR");
    String usd =
        api.post("/v1/accounts", "{\"name\":\"Dólares\",\"currency\":\"USD\"}", f.s().accessToken()).json("$.id");
    String id = create(f, "EXPENSE", 100, "2026-10-01").json("$.id");
    Response updated =
        api.put("/v1/transactions/" + id, body(usd, f.expenseCat(), "EXPENSE", 250, "2026-10-05", "Cambio"), f.s().accessToken());
    assertThat(updated.status()).isEqualTo(200);
    assertThat((String) updated.json("$.currency")).isEqualTo("USD");
    assertThat((Integer) updated.json("$.amount")).isEqualTo(250);
    assertThat(((Number) api.get("/v1/accounts/" + f.account(), f.s().accessToken()).json("$.balance")).longValue())
        .isEqualTo(10_000);
    assertThat(api.delete("/v1/transactions/" + id, null, f.s().accessToken()).status()).isEqualTo(204);
    assertThat(api.get("/v1/transactions/" + id, f.s().accessToken()).status()).isEqualTo(404);
  }

  @Test
  void accountsAndCategoriesInUseCannotBeDeleted() {
    Fixture f = fixture("EUR");
    create(f, "EXPENSE", 100, "2026-10-01");
    Response account = api.delete("/v1/accounts/" + f.account(), null, f.s().accessToken());
    assertThat(account.status()).isEqualTo(409);
    assertThat((String) account.json("$.code")).isEqualTo("ACCOUNT_NOT_EMPTY");
    Response category = api.delete("/v1/categories/" + f.expenseCat(), null, f.s().accessToken());
    assertThat(category.status()).isEqualTo(409);
    assertThat((String) category.json("$.code")).isEqualTo("CATEGORY_IN_USE");
  }

  @Test
  void deletingTheUserRemovesEverything() {
    Fixture f = fixture("EUR");
    create(f, "EXPENSE", 100, "2026-10-01");
    assertThat(api.delete("/v1/me", "{\"password\":\"" + PASSWORD + "\"}", f.s().accessToken()).status())
        .isEqualTo(204);
  }
}
