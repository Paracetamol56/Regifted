package com.regifted.app.security;

import com.regifted.app.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockCustomUser> {

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    User user = new User();
    user.setEmail(annotation.username());
    user.setPassword("password");

    CustomUserPrincipal principal = new CustomUserPrincipal(user);

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        principal,
        null,
        principal.getAuthorities());

    context.setAuthentication(authentication);
    return context;
  }
}
