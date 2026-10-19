package com.moneytracker.accounts;

import com.moneytracker.accounts.AccountDtos.AccountResponse;
import com.moneytracker.accounts.AccountDtos.CreateAccountRequest;
import com.moneytracker.accounts.AccountDtos.UpdateAccountRequest;
import com.moneytracker.core.security.CurrentUser;
import com.moneytracker.core.web.PageParams;
import com.moneytracker.core.web.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

  private final AccountService accounts;

  public AccountController(AccountService accounts) {
    this.accounts = accounts;
  }

  @GetMapping
  public PageResponse<AccountResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "" + PageParams.DEFAULT_LIMIT) int limit) {
    return accounts.list(
        CurrentUser.id(), PageParams.of(page, limit, Sort.by("name").and(Sort.by("id"))));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
    return accounts.create(CurrentUser.id(), request);
  }

  @GetMapping("/{id}")
  public AccountResponse get(@PathVariable UUID id) {
    return accounts.get(CurrentUser.id(), id);
  }

  @PatchMapping("/{id}")
  public AccountResponse update(
      @PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) {
    return accounts.update(CurrentUser.id(), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    accounts.delete(CurrentUser.id(), id);
  }
}
