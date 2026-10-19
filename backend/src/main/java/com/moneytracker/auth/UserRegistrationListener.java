package com.moneytracker.auth;

import com.moneytracker.users.User;

/** Permite a otros módulos preparar datos iniciales al registrarse un usuario. */
public interface UserRegistrationListener {

  void onUserRegistered(User user);
}
