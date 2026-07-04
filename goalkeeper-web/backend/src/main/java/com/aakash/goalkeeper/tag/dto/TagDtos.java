package com.aakash.goalkeeper.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class TagDtos {
    public record TagCreateRequest(@NotBlank @Size(max = 50) String name) {}

    public record TagDto(UUID id, String name) {}
}
