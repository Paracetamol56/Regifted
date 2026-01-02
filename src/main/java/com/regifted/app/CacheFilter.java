package com.regifted.app;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CacheFilter implements Filter {

  private static final Map<String, String> CACHE_RULES = Map.of(
      "/items", "public, max-age=300",
      "/keywords", "public, max-age=3600",
      "/states", "public, max-age=3600",
      "/messages", "no-store, no-cache",
      "/searches", "no-cache, must-revalidate",
      "/users/me", "no-cache, must-revalidate",
      "/users", "private, max-age=300");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String path = httpRequest.getRequestURI();

    CACHE_RULES.forEach((pattern, cacheHeader) -> {
      if (path.startsWith(pattern)) {
        httpResponse.setHeader("Cache-Control", cacheHeader);
      }
    });

    chain.doFilter(request, response);
  }
}
