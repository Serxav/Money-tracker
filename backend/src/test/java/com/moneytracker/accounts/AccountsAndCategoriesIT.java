package com.moneytracker.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import com.moneytracker.support.ApiClient.Response;
import com.moneytracker.support.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;

class AccountsAndCategoriesIT extends IntegrationTest {

  private String createAccount(Session s, String name, String currency, long initial) {
    Response res =
        api.post(
            "/v1/accounts",
            "{\"name\":\"" + name + "\",\"currency\":\"" + currency + "\",\"initialBalance\":" + initial + "}",
            s.accessToken());
    assertThat(res.status()).as(res.body()).isEqualTo(201);
    return res.json("$.id");
  }

  @Test
  void createAndListAccountsWithCurrencyDigits() {
    Session s = register();
    createAccount(s, "Banco", "EUR", 150_000);
    createAccount(s, "Viaje Japón", "JPY", 20_000);
    createAccount(s, "Kuwait", "KWD", 1_500);

    Response list = api.get("/v1/accounts", s.accessToken());
    assertThat(list.status()).isEqualTo(200);
    assertThat((Integer) list.json("$.total")).isEqualTo(3);
    List<String> names = list.json("$.items[*].name");
    assertThat(names).containsExactly("Banco", "Kuwait", "Viaje Japón");
    List<Integer> digits = list.json("$.items[*].fractionDigits");
    assertThat(digits).containsExactly(2, 3, 0);
    List<Integer> balances = list.json("$.items[*].balance");
    assertThat(balances).containsExactly(150_000, 1_500, 20_000);
  }

  @Test
  void accountNamesAreUniquePerUserIgnoringCase() {
    Session s = register();
    createAccount(s, "Banco", "EUR", 0);
    Response dup = api.post("/v1/accounts", "{\"name\":\"banco\",\"currency\":\"USD\"}", s.accessToken());
    assertThat(dup.status()).isEqualTo(409);
    assertThat((String) dup.json("$.code")).isEqualTo("ACCOUNT_NAME_TAKEN");
    // Otro usuario puede usar el mismo nombre.
    createAccount(register(), "Banco", "EUR", 0);
  }

  @Test
  void accountCurrencyCannotBeChangedAndMustBeSupported() {
    Session s = register();
    String id = createAccount(s, "Banco", "EUR", 0);
    assertThat(api.patch("/v1/accounts/" + id, "{\"currency\":\"USD\"}", s.accessToken()).status())
        .isEqualTo(400);
    Response bad = api.post("/v1/accounts", "{\"name\":\"X\",\"currency\":\"eur\"}", s.accessToken());
    assertThat(bad.status()).isEqualTo(400);
    Response unsupported = api.post("/v1/accounts", "{\"name\":\"X\",\"currency\":\"SEK\"}", s.accessToken());
    assertThat(unsupported.status()).isEqualTo(422);
  }

  @Test
  void accountCanBeRenamedAndDeleted() {
    Session s = register();
    String id = createAccount(s, "Banco", "EUR", 0);
    Response renamed =
        api.patch("/v1/accounts/" + id, "{\"name\":\"Banco principal\",\"initialBalance\":500}", s.accessToken());
    assertThat(renamed.status()).isEqualTo(200);
    assertThat((String) renamed.json("$.name")).isEqualTo("Banco principal");
    assertThat((Integer) renamed.json("$.balance")).isEqualTo(500);
    assertThat(api.delete("/v1/accounts/" + id, null, s.accessToken()).status()).isEqualTo(204);
    assertThat(api.get("/v1/accounts/" + id, s.accessToken()).status()).isEqualTo(404);
  }

  @Test
  void usersCannotSeeOrModifyOtherUsersAccounts() {
    Session owner = register();
    Session attacker = register();
    String id = createAccount(owner, "Banco", "EUR", 1000);

    assertThat(api.get("/v1/accounts/" + id, attacker.accessToken()).status()).isEqualTo(404);
    assertThat(api.patch("/v1/accounts/" + id, "{\"name\":\"Mío\"}", attacker.accessToken()).status())
        .isEqualTo(404);
    assertThat(api.delete("/v1/accounts/" + id, null, attacker.accessToken()).status()).isEqualTo(404);
    assertThat((Integer) api.get("/v1/accounts", attacker.accessToken()).json("$.total")).isZero();
    assertThat((String) api.get("/v1/accounts/" + id, owner.accessToken()).json("$.name")).isEqualTo("Banco");
  }

  @Test
  void ownerCannotBeInjectedThroughTheBody() {
    Session s = register();
    Response res =
        api.post(
            "/v1/accounts",
            "{\"name\":\"X\",\"currency\":\"EUR\",\"userId\":\"" + register().userId() + "\"}",
            s.accessToken());
    assertThat(res.status()).isEqualTo(400);
  }

  @Test
  void paginationLimitsAreEnforced() {
    Session s = register();
    assertThat(api.get("/v1/accounts?limit=101", s.accessToken()).status()).isEqualTo(400);
    assertThat(api.get("/v1/accounts?page=-1", s.accessToken()).status()).isEqualTo(400);
    assertThat(api.get("/v1/accounts?limit=abc", s.accessToken()).status()).isEqualTo(400);
  }

  @Test
  void newUsersGetDefaultCategories() {
    Session s = register();
    Response all = api.get("/v1/categories", s.accessToken());
    assertThat(all.status()).isEqualTo(200);
    assertThat((Integer) all.json("$.total")).isEqualTo(8);
    Response incomes = api.get("/v1/categories?type=INCOME", s.accessToken());
    List<String> names = incomes.json("$.items[*].name");
    assertThat(names).containsExactly("Nómina", "Otros ingresos");
  }

  @Test
  void categoriesCanBeCreatedUpdatedAndDeleted() {
    Session s = register();
    Response created =
        api.post(
            "/v1/categories",
            "{\"name\":\"Mascotas\",\"type\":\"EXPENSE\",\"icon\":\"pet\",\"color\":\"#AABBCC\"}",
            s.accessToken());
    assertThat(created.status()).isEqualTo(201);
    String id = created.json("$.id");
    assertThat(api.post("/v1/categories", "{\"name\":\"mascotas\",\"type\":\"EXPENSE\"}", s.accessToken()).status())
        .isEqualTo(409);
    // Mismo nombre con otro tipo sí está permitido.
    assertThat(api.post("/v1/categories", "{\"name\":\"Mascotas\",\"type\":\"INCOME\"}", s.accessToken()).status())
        .isEqualTo(201);

    Response updated = api.patch("/v1/categories/" + id, "{\"color\":\"#000000\"}", s.accessToken());
    assertThat((String) updated.json("$.color")).isEqualTo("#000000");
    assertThat(api.patch("/v1/categories/" + id, "{\"type\":\"INCOME\"}", s.accessToken()).status())
        .isEqualTo(400);
    assertThat(api.patch("/v1/categories/" + id, "{\"color\":\"red\"}", s.accessToken()).status())
        .isEqualTo(400);
    assertThat(api.delete("/v1/categories/" + id, null, s.accessToken()).status()).isEqualTo(204);
  }

  @Test
  void usersCannotModifyOtherUsersCategories() {
    Session owner = register();
    Session attacker = register();
    String id = api.get("/v1/categories", owner.accessToken()).json("$.items[0].id");
    assertThat(api.patch("/v1/categories/" + id, "{\"name\":\"X\"}", attacker.accessToken()).status())
        .isEqualTo(404);
    assertThat(api.delete("/v1/categories/" + id, null, attacker.accessToken()).status()).isEqualTo(404);
  }

  @Test
  void deletingTheUserRemovesAccountsAndCategories() {
    Session s = register();
    createAccount(s, "Banco", "EUR", 0);
    assertThat(api.delete("/v1/me", "{\"password\":\"" + PASSWORD + "\"}", s.accessToken()).status())
        .isEqualTo(204);
  }
}
