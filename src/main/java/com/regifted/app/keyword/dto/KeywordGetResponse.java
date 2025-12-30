package com.regifted.app.keyword.dto;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.keyword.Keyword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordGetResponse {
  private String uuid;
  private String href;
  private String name;

  public static KeywordGetResponse from(Keyword keyword) {
    KeywordGetResponse response = new KeywordGetResponse();
    response.setUuid(keyword.getUuid());
    response.setName(keyword.getName());
    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/keywords/{uuid}")
        .buildAndExpand(keyword.getUuid())
        .toUriString());
    return response;
  }
}
