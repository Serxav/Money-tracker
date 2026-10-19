package com.moneytracker.users;

import com.moneytracker.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/me")
public class MeController {

  private final UserService users;

  public MeController(UserService users) {
    this.users = users;
  }

  @GetMapping
  public UserResponse me() {
    return UserResponse.of(users.get(CurrentUser.id()));
  }

  @PatchMapping
  public UserResponse update(@Valid @RequestBody UpdateMeRequest request) {
    return UserResponse.of(users.update(CurrentUser.id(), request));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@Valid @RequestBody DeleteMeRequest request) {
    users.delete(CurrentUser.id(), request.password());
  }
}
