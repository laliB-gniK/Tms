package com.translation.controller;

import com.translation.dto.LanguageDto;
import com.translation.model.Language;
import com.translation.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
@Tag(name = "Languages", description = "Language management endpoints")
public class LanguageController {

    private final LanguageService languageService;

    @PostMapping
    @Operation(summary = "Create a new language")
    public ResponseEntity<Language> createLanguage(@Valid @RequestBody LanguageDto languageDto) {
        Language language = languageService.createLanguage(languageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(language);
    }

    @PutMapping("/{code}")
    @Operation(summary = "Update an existing language")
    public ResponseEntity<Language> updateLanguage(
            @Parameter(description = "Language code", required = true)
            @PathVariable String code,
            @Valid @RequestBody LanguageDto languageDto) {
        Language language = languageService.updateLanguage(code, languageDto);
        return ResponseEntity.ok(language);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get a language by code")
    public ResponseEntity<Language> getLanguage(
            @Parameter(description = "Language code", required = true)
            @PathVariable String code) {
        Language language = languageService.getLanguage(code);
        return ResponseEntity.ok(language);
    }

    @GetMapping
    @Operation(summary = "Get all languages")
    public ResponseEntity<List<Language>> getAllLanguages() {
        List<Language> languages = languageService.getAllLanguages();
        return ResponseEntity.ok(languages);
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Delete a language")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLanguage(
            @Parameter(description = "Language code", required = true)
            @PathVariable String code) {
        languageService.deleteLanguage(code);
    }
}
