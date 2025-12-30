package com.regifted.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // 1. Ressources statiques
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

            // 2. Routes publiques
            .requestMatchers("/", "/register", "/login").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/users").permitAll()
            .requestMatchers("/items/*/messages").authenticated()

            // 3. Navigation libre en lecture (GET)
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/**").permitAll()
            // 4. Tout le reste demande une connexion
            .anyRequest().authenticated())
        // Utilisation de Customizer.withDefaults() ou Lambda pour éviter la
        // dépréciation
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .permitAll())
        // Correction de l'enchaînement : on ferme bien la parenthèse avant de passer au
        // suivant
        .httpBasic(org.springframework.security.config.Customizer.withDefaults())

        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll())
        .userDetailsService(userDetailsService);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
