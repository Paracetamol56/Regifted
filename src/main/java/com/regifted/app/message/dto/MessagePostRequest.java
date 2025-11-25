package com.regifted.app.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessagePostRequest {
  @NotBlank(message = "Receiver UUID is required")
  private String receiver;

  @NotBlank(message = "Item UUID is required")
  private String item;

  @NotBlank(message = "Content is required")
  @Size(max = 1000, message = "Content must be at most 1000 characters")
  private String content;
}
