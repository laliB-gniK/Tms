package com.translation.service;

import com.translation.dto.LanguageDto;
import com.translation.model.Language;
import com.translation.repository.LanguageRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageService languageService;

    private Language language;
    private LanguageDto languageDto;

    @BeforeEach
    void setUp() {
        language = new Language();
        language.setId(1L);
        language.setCode("en");
        language.setName("English");

        languageDto = new LanguageDto();
        languageDto.setCode("en");
        languageDto.setName("English");
    }

    @Test
    void createLanguage_Success() {
        when(languageRepository.existsByCode("en")).thenReturn(false);
        when(languageRepository.save(any(Language.class))).thenReturn(language);

        Language result = languageService.createLanguage(languageDto);

        assertNotNull(result);
        assertEquals("en", result.getCode());
        assertEquals("English", result.getName());
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void createLanguage_AlreadyExists() {
        when(languageRepository.existsByCode("en")).thenReturn(true);

        assertThrows(EntityExistsException.class, () -> {
            languageService.createLanguage(languageDto);
        });

        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void updateLanguage_Success() {
        LanguageDto updateDto = new LanguageDto();
        updateDto.setCode("en");
        updateDto.setName("Updated English");

        when(languageRepository.findByCode("en")).thenReturn(Optional.of(language));
        when(languageRepository.save(any(Language.class))).thenReturn(language);

        Language result = languageService.updateLanguage("en", updateDto);

        assertNotNull(result);
        assertEquals("en", result.getCode());
        assertEquals("Updated English", result.getName());
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void updateLanguage_NotFound() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            languageService.updateLanguage("en", languageDto);
        });

        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void updateLanguage_CodeChangeConflict() {
        LanguageDto updateDto = new LanguageDto();
        updateDto.setCode("fr");
        updateDto.setName("French");

        when(languageRepository.findByCode("en")).thenReturn(Optional.of(language));
        when(languageRepository.existsByCode("fr")).thenReturn(true);

        assertThrows(EntityExistsException.class, () -> {
            languageService.updateLanguage("en", updateDto);
        });

        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void getLanguage_Success() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(language));

        Language result = languageService.getLanguage("en");

        assertNotNull(result);
        assertEquals("en", result.getCode());
        assertEquals("English", result.getName());
    }

    @Test
    void getLanguage_NotFound() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            languageService.getLanguage("en");
        });
    }

    @Test
    void getAllLanguages_Success() {
        Language language2 = new Language();
        language2.setId(2L);
        language2.setCode("fr");
        language2.setName("French");

        when(languageRepository.findAll()).thenReturn(Arrays.asList(language, language2));

        List<Language> result = languageService.getAllLanguages();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("en", result.get(0).getCode());
        assertEquals("fr", result.get(1).getCode());
    }

    @Test
    void deleteLanguage_Success() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(language));

        languageService.deleteLanguage("en");

        verify(languageRepository).delete(language);
    }

    @Test
    void deleteLanguage_NotFound() {
        when(languageRepository.findByCode("en")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            languageService.deleteLanguage("en");
        });

        verify(languageRepository, never()).delete(any(Language.class));
    }

    @Test
    void deleteLanguage_WithTranslations() {
        language.getTranslations().add(new com.translation.model.Translation());
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(language));

        assertThrows(IllegalStateException.class, () -> {
            languageService.deleteLanguage("en");
        });

        verify(languageRepository, never()).delete(any(Language.class));
    }
}
