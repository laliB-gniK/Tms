package com.translation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LanguageDto {
    @NotBlank(message = "Language code is required")
    @Pattern(regexp = "^[a-z]{2,3}(?:-[A-Z]{2,3}(?:-[a-zA-Z]{4})?)?$", 
            message = "Invalid language code format. Use ISO format (e.g., 'en', 'en-US')")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    private String code;
    
    @NotBlank(message = "Language name is required")
    @Size(min = 2, max = 50, message = "Language name must be between 2 and 50 characters")
    private String name;
}
