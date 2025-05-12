package com.translation.config;

import com.translation.dto.LanguageDto;
import com.translation.dto.TranslationDto;
import com.translation.service.LanguageService;
import com.translation.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class TranslationDataLoader implements CommandLineRunner {

    private final LanguageService languageService;
    private final TranslationService translationService;
    private final Random random = new Random();

    private static final String[] LANGUAGES = {"en", "fr", "es", "de", "it"};
    private static final String[] LANGUAGE_NAMES = {"English", "French", "Spanish", "German", "Italian"};
    private static final String[] CATEGORIES = {"common", "error", "success", "validation", "navigation"};
    private static final String[] SECTIONS = {"header", "footer", "sidebar", "main", "form"};
    private static final String[] TAGS = {"mobile", "web", "desktop"};

    @Override
    public void run(String... args) {
        log.info("Starting to load sample translations...");
        createLanguages();
        createTranslations();
        log.info("Finished loading sample translations.");
    }

    private void createLanguages() {
        for (int i = 0; i < LANGUAGES.length; i++) {
            LanguageDto languageDto = new LanguageDto();
            languageDto.setCode(LANGUAGES[i]);
            languageDto.setName(LANGUAGE_NAMES[i]);
            try {
                languageService.createLanguage(languageDto);
                log.info("Created language: {}", LANGUAGES[i]);
            } catch (Exception e) {
                log.error("Error creating language {}: {}", LANGUAGES[i], e.getMessage());
            }
        }
    }

    private void createTranslations() {
        int totalTranslations = 100_000; // Create 100k translations
        int batchSize = 1000;
        int created = 0;

        while (created < totalTranslations) {
            for (int i = 0; i < batchSize && created < totalTranslations; i++) {
                for (String language : LANGUAGES) {
                    try {
                        createRandomTranslation(language, created);
                        created++;
                    } catch (Exception e) {
                        log.error("Error creating translation: {}", e.getMessage());
                    }
                }
            }
            log.info("Created {} translations", created);
        }
    }

    private void createRandomTranslation(String languageCode, int index) {
        String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
        String section = SECTIONS[random.nextInt(SECTIONS.length)];
        String key = String.format("%s.%s.key%d", category, section, index);
        
        TranslationDto dto = new TranslationDto();
        dto.setKey(key);
        dto.setLanguageCode(languageCode);
        dto.setContent(generateContent(languageCode, category, section, index));
        dto.setTags(generateRandomTags());
        
        translationService.createTranslation(dto);
    }

    private String generateContent(String language, String category, String section, int index) {
        return String.format("[%s] %s - %s content #%d", 
            language.toUpperCase(), 
            category, 
            section, 
            index
        );
    }

    private Set<String> generateRandomTags() {
        Set<String> selectedTags = new HashSet<>();
        int numTags = random.nextInt(TAGS.length) + 1;
        while (selectedTags.size() < numTags) {
            selectedTags.add(TAGS[random.nextInt(TAGS.length)]);
        }
        return selectedTags;
    }
}
