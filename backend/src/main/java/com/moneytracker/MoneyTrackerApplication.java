package com.moneytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoneyTrackerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoneyTrackerApplication.class, args);
  }
}
