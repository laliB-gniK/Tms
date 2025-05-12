package com.translation.service;

import com.translation.dto.TranslationDto;
import com.translation.model.Language;
import com.translation.model.Tag;
import com.translation.model.Translation;
import com.translation.repository.LanguageRepository;
import com.translation.repository.TagRepository;
import com.translation.repository.TranslationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TranslationService translationService;

    private Language language;
    private Translation translation;
    private TranslationDto translationDto;
    private Set<Tag> tags;

    @BeforeEach
    void setUp() {
        language = new Language();
        language.setId(1L);
        language.setCode("en");
        language.setName("English");

        tags = new HashSet<>();
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("web");
        tags.add(tag);

        translation = new Translation();
        translation.setId(1L);
        translation.setTranslationKey("test.key");
        translation.setContent("Test content");
        translation.setLanguage(language);
        translation.setTags(tags);

        translationDto = new TranslationDto();
        translationDto.setKey("test.key");
        translationDto.setContent("Test content");
        translationDto.setLanguageCode("en");
        translationDto.setTags(Set.of("web"));
    }

    @Test
    void createTranslation_Success() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(language));
        when(tagRepository.findByNameIn(any())).thenReturn(tags);
        when(translationRepository.save(any(Translation.class))).thenReturn(translation);

        Translation result = translationService.createTranslation(translationDto);

        assertNotNull(result);
        assertEquals("test.key", result.getTranslationKey());
        assertEquals("Test content", result.getContent());
        assertEquals("en", result.getLanguage().getCode());
        verify(translationRepository).save(any(Translation.class));
    }

    @Test
    void createTranslation_LanguageNotFound() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            translationService.createTranslation(translationDto);
        });

        verify(translationRepository, never()).save(any(Translation.class));
    }

    @Test
    void getTranslation_Success() {
        when(translationRepository.findByKeyAndLanguageCode("test.key", "en"))
            .thenReturn(Optional.of(translation));

        Translation result = translationService.getTranslation("test.key", "en");

        assertNotNull(result);
        assertEquals("test.key", result.getTranslationKey());
        assertEquals("Test content", result.getContent());
    }

    @Test
    void getTranslation_NotFound() {
        when(translationRepository.findByKeyAndLanguageCode("test.key", "en"))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            translationService.getTranslation("test.key", "en");
        });
    }

    @Test
    void searchTranslations_Success() {
        List<Translation> translations = List.of(translation);
        Page<Translation> page = new PageImpl<>(translations);
        
        when(translationRepository.searchTranslations(anyString(), anySet(), any(PageRequest.class)))
            .thenReturn(page);

        Page<Translation> result = translationService.searchTranslations(
            "test",
            Set.of("web"),
            0,
            10
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("test.key", result.getContent().get(0).getTranslationKey());
    }

    @Test
    void getTranslationsForLanguage_Success() {
        List<Translation> translations = Arrays.asList(
            createTranslation("common.button.save", "Save"),
            createTranslation("common.button.cancel", "Cancel")
        );

        when(translationRepository.findAllByLanguageCode("en")).thenReturn(translations);

        Map<String, Object> result = translationService.getTranslationsForLanguage("en");

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> common = (Map<String, Object>) result.get("common");
        @SuppressWarnings("unchecked")
        Map<String, Object> button = (Map<String, Object>) common.get("button");
        assertEquals("Save", button.get("save"));
        assertEquals("Cancel", button.get("cancel"));
    }

    private Translation createTranslation(String key, String content) {
        Translation t = new Translation();
        t.setTranslationKey(key);
        t.setContent(content);
        t.setLanguage(language);
        return t;
    }
}
