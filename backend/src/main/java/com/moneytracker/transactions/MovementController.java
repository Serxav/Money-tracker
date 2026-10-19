package com.moneytracker.transactions;

import com.moneytracker.categories.CategoryType;
import com.moneytracker.core.security.CurrentUser;
import com.moneytracker.core.web.PageParams;
import com.moneytracker.core.web.PageResponse;
import com.moneytracker.transactions.MovementDtos.MovementFilter;
import com.moneytracker.transactions.MovementDtos.MovementRequest;
import com.moneytracker.transactions.MovementDtos.MovementResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
public class MovementController {

  private static final Sort NEWEST_FIRST =
      Sort.by(Sort.Order.desc("date"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  private final MovementService movements;

  public MovementController(MovementService movements) {
    this.movements = movements;
  }

  @GetMapping
  public PageResponse<MovementResponse> list(
      @RequestParam(required = false) UUID accountId,
      @RequestParam(required = false) UUID categoryId,
      @RequestParam(required = false) CategoryType type,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "" + PageParams.DEFAULT_LIMIT) int limit) {
    return movements.list(
        CurrentUser.id(),
        new MovementFilter(accountId, categoryId, type, from, to),
        PageParams.of(page, limit, NEWEST_FIRST));
  }

  /**
   * Con {@code Idempotency-Key}, un reintento con los mismos datos devuelve el movimiento ya
   * creado (200 e {@code Idempotent-Replayed: true}) en lugar de duplicarlo.
   */
  @PostMapping
  public ResponseEntity<MovementResponse> create(
      @Valid @RequestBody MovementRequest request,
      @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
    MovementService.CreateResult result =
        movements.create(CurrentUser.id(), request, idempotencyKey);
    if (result.replayed()) {
      return ResponseEntity.ok().header("Idempotent-Replayed", "true").body(result.movement());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(result.movement());
  }

  @GetMapping("/{id}")
  public MovementResponse get(@PathVariable UUID id) {
    return movements.get(CurrentUser.id(), id);
  }

  @PutMapping("/{id}")
  public MovementResponse update(@PathVariable UUID id, @Valid @RequestBody MovementRequest request) {
    return movements.update(CurrentUser.id(), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    movements.delete(CurrentUser.id(), id);
  }
}
