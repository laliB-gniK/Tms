package com.translation.integration;

import com.translation.dto.LanguageDto;
import com.translation.dto.TranslationDto;
import com.translation.model.Translation;
import com.translation.repository.LanguageRepository;
import com.translation.repository.TranslationRepository;
import com.translation.service.LanguageService;
import com.translation.service.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TranslationIntegrationTest {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @BeforeEach
    void setUp() {
        translationRepository.deleteAll();
        languageRepository.deleteAll();

        // Create test languages
        createLanguage("en", "English");
        createLanguage("fr", "French");
    }

    @Test
    void testCompleteTranslationFlow() {
        // Create translation
        TranslationDto translationDto = new TranslationDto();
        translationDto.setKey("common.button.save");
        translationDto.setContent("Save");
        translationDto.setLanguageCode("en");
        translationDto.setTags(Set.of("web", "mobile"));

        Translation savedTranslation = translationService.createTranslation(translationDto);
        assertNotNull(savedTranslation);
        assertEquals("common.button.save", savedTranslation.getTranslationKey());
        assertEquals(2, savedTranslation.getTags().size());

        // Create another translation
        translationDto.setKey("common.button.cancel");
        translationDto.setContent("Cancel");
        translationService.createTranslation(translationDto);

        // Search translations
        Page<Translation> searchResult = translationService.searchTranslations(
            "button",
            Set.of("web"),
            0,
            10
        );
        assertEquals(2, searchResult.getContent().size());

        // Export translations
        Map<String, Object> exportResult = translationService.getTranslationsForLanguage("en");
        assertNotNull(exportResult);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> common = (Map<String, Object>) exportResult.get("common");
        @SuppressWarnings("unchecked")
        Map<String, Object> button = (Map<String, Object>) common.get("button");
        
        assertEquals("Save", button.get("save"));
        assertEquals("Cancel", button.get("cancel"));

        // Update translation
        translationDto.setKey("common.button.save");
        translationDto.setContent("Save Changes");
        Translation updatedTranslation = translationService.updateTranslation(
            "common.button.save",
            "en",
            translationDto
        );
        assertEquals("Save Changes", updatedTranslation.getContent());

        // Verify cache invalidation by retrieving updated translation
        Translation retrievedTranslation = translationService.getTranslation(
            "common.button.save",
            "en"
        );
        assertEquals("Save Changes", retrievedTranslation.getContent());
    }

    @Test
    void testMultiLanguageSupport() {
        // Create translations for both languages
        createTranslation("common.greeting", "Hello", "en");
        createTranslation("common.greeting", "Bonjour", "fr");

        // Verify each language's translations
        Translation enTranslation = translationService.getTranslation("common.greeting", "en");
        Translation frTranslation = translationService.getTranslation("common.greeting", "fr");

        assertEquals("Hello", enTranslation.getContent());
        assertEquals("Bonjour", frTranslation.getContent());

        // Verify exports for each language
        Map<String, Object> enExport = translationService.getTranslationsForLanguage("en");
        Map<String, Object> frExport = translationService.getTranslationsForLanguage("fr");

        @SuppressWarnings("unchecked")
        Map<String, Object> enCommon = (Map<String, Object>) enExport.get("common");
        @SuppressWarnings("unchecked")
        Map<String, Object> frCommon = (Map<String, Object>) frExport.get("common");

        assertEquals("Hello", enCommon.get("greeting"));
        assertEquals("Bonjour", frCommon.get("greeting"));
    }

    private void createLanguage(String code, String name) {
        LanguageDto languageDto = new LanguageDto();
        languageDto.setCode(code);
        languageDto.setName(name);
        languageService.createLanguage(languageDto);
    }

    private void createTranslation(String key, String content, String languageCode) {
        TranslationDto dto = new TranslationDto();
        dto.setKey(key);
        dto.setContent(content);
        dto.setLanguageCode(languageCode);
        dto.setTags(Set.of("test"));
        translationService.createTranslation(dto);
    }
}
