package com.regifted.app.message.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessagePostRequest {
  @NotBlank(message = "Content is required")
  @Size(max = 1000, message = "Content must be at most 1000 characters")
  private String content;

  private UUID receiver;
}
