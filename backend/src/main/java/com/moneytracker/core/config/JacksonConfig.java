package com.moneytracker.core.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/**
 * Tipos JSON estrictos: un número no se acepta como texto ni al revés, ni un texto como booleano.
 * Evita que la API "adivine" valores que el cliente no envió bien.
 */
@Configuration
public class JacksonConfig {

  @Bean
  JsonMapperBuilderCustomizer strictCoercion() {
    return builder ->
        builder
            .withCoercionConfig(
                LogicalType.Integer,
                c ->
                    c.setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
            .withCoercionConfig(
                LogicalType.Boolean,
                c ->
                    c.setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail))
            .withCoercionConfig(
                LogicalType.Textual,
                c ->
                    c.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
  }
}
