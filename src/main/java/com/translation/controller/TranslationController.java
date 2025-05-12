package com.translation.controller;

import com.translation.dto.SearchRequestDto;
import com.translation.dto.TranslationDto;
import com.translation.model.Translation;
import com.translation.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/translations")
@RequiredArgsConstructor
@Tag(name = "Translations", description = "Translation management endpoints")
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    @Operation(summary = "Create a new translation")
    public ResponseEntity<Translation> createTranslation(@Valid @RequestBody TranslationDto translationDto) {
        Translation translation = translationService.createTranslation(translationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(translation);
    }

    @PutMapping("/{key}/{languageCode}")
    @Operation(summary = "Update an existing translation")
    public ResponseEntity<Translation> updateTranslation(
            @Parameter(description = "Translation key", required = true) @PathVariable String key,
            @Parameter(description = "Language code", required = true) @PathVariable String languageCode,
            @Valid @RequestBody TranslationDto translationDto) {
        Translation translation = translationService.updateTranslation(key, languageCode, translationDto);
        return ResponseEntity.ok(translation);
    }

    @GetMapping("/{key}/{languageCode}")
    @Operation(summary = "Get a translation by key and language code")
    public ResponseEntity<Translation> getTranslation(
            @Parameter(description = "Translation key", required = true) @PathVariable String key,
            @Parameter(description = "Language code", required = true) @PathVariable String languageCode) {
        Translation translation = translationService.getTranslation(key, languageCode);
        return ResponseEntity.ok(translation);
    }

    @DeleteMapping("/{key}/{languageCode}")
    @Operation(summary = "Delete a translation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTranslation(
            @Parameter(description = "Translation key", required = true) @PathVariable String key,
            @Parameter(description = "Language code", required = true) @PathVariable String languageCode) {
        translationService.deleteTranslation(key, languageCode);
    }

    @PostMapping("/search")
    @Operation(summary = "Search translations with pagination")
    public ResponseEntity<Page<Translation>> searchTranslations(@Valid @RequestBody SearchRequestDto searchRequest) {
        Page<Translation> translations = translationService.searchTranslations(
            searchRequest.getSearchTerm(),
            searchRequest.getTags(),
            searchRequest.getPage(),
            searchRequest.getSize()
        );
        return ResponseEntity.ok(translations);
    }

    @GetMapping("/export/{languageCode}")
    @Operation(summary = "Export translations for a specific language in a nested JSON structure")
    public ResponseEntity<Map<String, Object>> exportTranslations(
            @Parameter(description = "Language code", required = true) @PathVariable String languageCode) {
        Map<String, Object> translations = translationService.getTranslationsForLanguage(languageCode);
        return ResponseEntity.ok(translations);
    }
}
