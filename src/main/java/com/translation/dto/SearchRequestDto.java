package com.translation.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Set;

@Data
public class SearchRequestDto {
    private String searchTerm;
    private Set<String> tags;
    
    @Min(value = 0, message = "Page number cannot be negative")
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be greater than 0")
    private Integer size = 20;
}
