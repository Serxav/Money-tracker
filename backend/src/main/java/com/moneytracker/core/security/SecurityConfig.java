package com.moneytracker.core.security;

import com.moneytracker.core.error.ErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * API sin estado: todo requiere un access token JWT salvo registro, login, refresco, salud y
 * (solo si está activada) la documentación OpenAPI. Se deniega por defecto.
 */
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()) // API sin cookies: no aplica CSRF
        .cors(cors -> cors.disable()) // Sin cliente web por ahora
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(b -> b.disable())
        .formLogin(f -> f.disable())
        .logout(l -> l.disable())
        .headers(
            h ->
                h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                    .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                    .frameOptions(f -> f.deny()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        HttpMethod.POST,
                        "/v1/auth/register",
                        "/v1/auth/login",
                        "/v1/auth/refresh",
                        "/v1/auth/logout")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(
                        (req, res, ex) ->
                            ErrorResponseWriter.write(
                                res, 401, "UNAUTHORIZED", "Autenticación requerida o no válida"))
                    .accessDeniedHandler(
                        (req, res, ex) ->
                            ErrorResponseWriter.write(res, 403, "FORBIDDEN", "Acceso denegado")))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) ->
                            ErrorResponseWriter.write(
                                res, 401, "UNAUTHORIZED", "Autenticación requerida o no válida"))
                    .accessDeniedHandler(
                        (req, res, ex) ->
                            ErrorResponseWriter.write(res, 403, "FORBIDDEN", "Acceso denegado")));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }
}
