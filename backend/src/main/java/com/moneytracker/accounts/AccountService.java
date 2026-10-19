package com.moneytracker.accounts;

import com.moneytracker.accounts.AccountDtos.AccountResponse;
import com.moneytracker.accounts.AccountDtos.CreateAccountRequest;
import com.moneytracker.accounts.AccountDtos.UpdateAccountRequest;
import com.moneytracker.core.error.ApiException;
import com.moneytracker.core.web.PageResponse;
import com.moneytracker.currencies.CurrencyService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Todas las operaciones filtran por el usuario autenticado: otra cuenta responde 404. */
@Service
public class AccountService {

  private final AccountRepository accounts;
  private final CurrencyService currencies;
  private final AccountBalanceProvider balances;

  public AccountService(
      AccountRepository accounts, CurrencyService currencies, AccountBalanceProvider balances) {
    this.accounts = accounts;
    this.currencies = currencies;
    this.balances = balances;
  }

  @Transactional(readOnly = true)
  public PageResponse<AccountResponse> list(UUID userId, Pageable pageable) {
    Page<Account> page = accounts.findByUserId(userId, pageable);
    Map<UUID, Long> net =
        balances.netMovements(userId, page.getContent().stream().map(Account::getId).toList());
    return PageResponse.of(page, a -> toResponse(a, net.getOrDefault(a.getId(), 0L)));
  }

  @Transactional(readOnly = true)
  public AccountResponse get(UUID userId, UUID id) {
    Account account = require(userId, id);
    return toResponse(account, netOf(userId, account));
  }

  /** Para otros módulos: la cuenta solo existe si pertenece al usuario. */
  @Transactional(readOnly = true)
  public Account require(UUID userId, UUID id) {
    return accounts.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Cuenta"));
  }

  @Transactional
  public AccountResponse create(UUID userId, CreateAccountRequest request) {
    String name = request.name().trim();
    ensureNameAvailable(userId, name);
    Account account =
        accounts.save(
            new Account(
                userId,
                name,
                currencies.requireSupported(request.currency()),
                request.initialBalance() == null ? 0L : request.initialBalance()));
    return toResponse(account, 0L);
  }

  @Transactional
  public AccountResponse update(UUID userId, UUID id, UpdateAccountRequest request) {
    Account account = require(userId, id);
    if (request.name() != null) {
      String name = request.name().trim();
      if (name.isEmpty()) {
        throw ApiException.badRequest("VALIDATION_ERROR", "El nombre no puede estar vacío");
      }
      if (!name.equalsIgnoreCase(account.getName())) {
        ensureNameAvailable(userId, name);
      }
      account.setName(name);
    }
    if (request.initialBalance() != null) {
      account.setInitialBalance(request.initialBalance());
    }
    return toResponse(account, netOf(userId, account));
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    Account account = require(userId, id);
    if (balances.hasMovements(userId, id)) {
      throw ApiException.conflict(
          "ACCOUNT_NOT_EMPTY", "La cuenta tiene movimientos: bórralos o muévelos antes");
    }
    accounts.delete(account);
  }

  private void ensureNameAvailable(UUID userId, String name) {
    if (name.isEmpty()) {
      throw ApiException.badRequest("VALIDATION_ERROR", "El nombre no puede estar vacío");
    }
    if (accounts.existsByUserIdAndNameIgnoreCase(userId, name)) {
      throw ApiException.conflict("ACCOUNT_NAME_TAKEN", "Ya tienes una cuenta con ese nombre");
    }
  }

  private long netOf(UUID userId, Account account) {
    return balances.netMovements(userId, List.of(account.getId())).getOrDefault(account.getId(), 0L);
  }

  private AccountResponse toResponse(Account a, long net) {
    return new AccountResponse(
        a.getId(),
        a.getName(),
        a.getCurrency(),
        currencies.fractionDigits(a.getCurrency()),
        a.getInitialBalance(),
        Math.addExact(a.getInitialBalance(), net),
        a.getCreatedAt());
  }
}
