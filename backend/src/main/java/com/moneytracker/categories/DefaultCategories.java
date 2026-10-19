package com.moneytracker.categories;

import com.moneytracker.auth.UserRegistrationListener;
import com.moneytracker.users.User;
import java.util.List;
import org.springframework.stereotype.Component;

/** Crea categorías iniciales para que la app sea usable desde el primer momento. */
@Component
public class DefaultCategories implements UserRegistrationListener {

  private record Seed(String name, CategoryType type, String icon, String color) {}

  private static final List<Seed> SEEDS =
      List.of(
          new Seed("Comida", CategoryType.EXPENSE, "food", "#E07A5F"),
          new Seed("Transporte", CategoryType.EXPENSE, "transport", "#3D405B"),
          new Seed("Hogar", CategoryType.EXPENSE, "home", "#81B29A"),
          new Seed("Ocio", CategoryType.EXPENSE, "leisure", "#F2CC8F"),
          new Seed("Salud", CategoryType.EXPENSE, "health", "#5E9FA3"),
          new Seed("Otros gastos", CategoryType.EXPENSE, "other", "#9A8C98"),
          new Seed("Nómina", CategoryType.INCOME, "salary", "#2A9D8F"),
          new Seed("Otros ingresos", CategoryType.INCOME, "other", "#8AB17D"));

  private final CategoryRepository categories;

  public DefaultCategories(CategoryRepository categories) {
    this.categories = categories;
  }

  @Override
  public void onUserRegistered(User user) {
    categories.saveAll(
        SEEDS.stream()
            .map(s -> new Category(user.getId(), s.name(), s.type(), s.icon(), s.color()))
            .toList());
  }
}
