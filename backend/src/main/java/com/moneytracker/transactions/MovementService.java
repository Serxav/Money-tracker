package com.moneytracker.transactions;

import com.moneytracker.accounts.Account;
import com.moneytracker.accounts.AccountService;
import com.moneytracker.auth.TokenService;
import com.moneytracker.categories.Category;
import com.moneytracker.categories.CategoryService;
import com.moneytracker.core.error.ApiException;
import com.moneytracker.core.web.PageResponse;
import com.moneytracker.transactions.MovementDtos.MovementFilter;
import com.moneytracker.transactions.MovementDtos.MovementRequest;
import com.moneytracker.transactions.MovementDtos.MovementResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovementService {

  private static final Pattern IDEMPOTENCY_KEY = Pattern.compile("^[A-Za-z0-9_-]{8,64}$");
  static final LocalDate MIN_DATE = LocalDate.of(1970, 1, 1);

  private final MovementRepository movements;
  private final IdempotencyKeyRepository idempotencyKeys;
  private final AccountService accounts;
  private final CategoryService categories;

  public MovementService(
      MovementRepository movements,
      IdempotencyKeyRepository idempotencyKeys,
      AccountService accounts,
      CategoryService categories) {
    this.movements = movements;
    this.idempotencyKeys = idempotencyKeys;
    this.accounts = accounts;
    this.categories = categories;
  }

  public record CreateResult(MovementResponse movement, boolean replayed) {}

  @Transactional(readOnly = true)
  public PageResponse<MovementResponse> list(UUID userId, MovementFilter filter, Pageable pageable) {
    if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
      throw ApiException.badRequest("INVALID_DATE_RANGE", "La fecha inicial es posterior a la final");
    }
    return PageResponse.of(movements.findAll(spec(userId, filter), pageable), MovementResponse::of);
  }

  @Transactional(readOnly = true)
  public MovementResponse get(UUID userId, UUID id) {
    return MovementResponse.of(require(userId, id));
  }

  @Transactional
  public CreateResult create(UUID userId, MovementRequest request, String idempotencyKey) {
    String requestHash = null;
    if (idempotencyKey != null) {
      if (!IDEMPOTENCY_KEY.matcher(idempotencyKey).matches()) {
        throw ApiException.badRequest(
            "INVALID_IDEMPOTENCY_KEY", "Idempotency-Key: 8-64 caracteres [A-Za-z0-9_-]");
      }
      requestHash = hash(request);
      var existing = idempotencyKeys.findById(new IdempotencyKey.Id(userId, idempotencyKey));
      if (existing.isPresent()) {
        if (!existing.get().getRequestHash().equals(requestHash)) {
          throw ApiException.unprocessable(
              "IDEMPOTENCY_KEY_REUSED", "Esta Idempotency-Key ya se usó con otros datos");
        }
        return new CreateResult(MovementResponse.of(require(userId, existing.get().getTransactionId())), true);
      }
    }
    Movement draft = new Movement(userId);
    applyValidated(userId, draft, request);
    // save() devuelve la entidad gestionada (con createdAt/updatedAt): hay que usar esa.
    Movement movement = movements.saveAndFlush(draft);
    if (idempotencyKey != null) {
      idempotencyKeys.save(new IdempotencyKey(userId, idempotencyKey, requestHash, movement.getId()));
    }
    return new CreateResult(MovementResponse.of(movement), false);
  }

  @Transactional
  public MovementResponse update(UUID userId, UUID id, MovementRequest request) {
    Movement movement = require(userId, id);
    applyValidated(userId, movement, request);
    movements.flush();
    return MovementResponse.of(movement);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    movements.delete(require(userId, id));
  }

  private Movement require(UUID userId, UUID id) {
    return movements.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Movimiento"));
  }

  private void applyValidated(UUID userId, Movement movement, MovementRequest request) {
    Account account = accounts.require(userId, request.accountId());
    Category category = categories.require(userId, request.categoryId());
    if (category.getType() != request.type()) {
      throw ApiException.unprocessable(
          "CATEGORY_TYPE_MISMATCH", "La categoría no corresponde al tipo de movimiento");
    }
    LocalDate maxDate = LocalDate.now().plusYears(1);
    if (request.date().isBefore(MIN_DATE) || request.date().isAfter(maxDate)) {
      throw ApiException.unprocessable("INVALID_DATE", "Fecha fuera del rango permitido");
    }
    String description = request.description() == null ? null : request.description().strip();
    movement.apply(
        account.getId(),
        category.getId(),
        request.type(),
        request.amount(),
        account.getCurrency(),
        request.date(),
        description == null || description.isEmpty() ? null : description);
  }

  private static Specification<Movement> spec(UUID userId, MovementFilter f) {
    return (root, query, cb) -> {
      List<jakarta.persistence.criteria.Predicate> p = new ArrayList<>();
      p.add(cb.equal(root.get("userId"), userId));
      if (f.accountId() != null) {
        p.add(cb.equal(root.get("accountId"), f.accountId()));
      }
      if (f.categoryId() != null) {
        p.add(cb.equal(root.get("categoryId"), f.categoryId()));
      }
      if (f.type() != null) {
        p.add(cb.equal(root.get("type"), f.type()));
      }
      if (f.from() != null) {
        p.add(cb.greaterThanOrEqualTo(root.get("date"), f.from()));
      }
      if (f.to() != null) {
        p.add(cb.lessThanOrEqualTo(root.get("date"), f.to()));
      }
      return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
  }

  private static String hash(MovementRequest r) {
    return TokenService.sha256(
        String.join(
            "|",
            String.valueOf(r.accountId()),
            String.valueOf(r.categoryId()),
            String.valueOf(r.type()),
            String.valueOf(r.amount()),
            String.valueOf(r.date()),
            String.valueOf(r.description())));
  }
}
