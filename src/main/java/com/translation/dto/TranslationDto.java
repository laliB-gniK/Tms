package com.translation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class TranslationDto {
    @NotBlank(message = "Translation key is required")
    private String key;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotBlank(message = "Language code is required")
    private String languageCode;
    
    private Set<String> tags;
}
