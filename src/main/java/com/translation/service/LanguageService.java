package com.translation.service;

import com.translation.dto.LanguageDto;
import com.translation.model.Language;
import com.translation.repository.LanguageRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    @Transactional
    public Language createLanguage(LanguageDto dto) {
        if (languageRepository.existsByCode(dto.getCode())) {
            throw new EntityExistsException("Language already exists with code: " + dto.getCode());
        }

        Language language = new Language();
        language.setCode(dto.getCode().toLowerCase());
        language.setName(dto.getName());

        log.info("Creating new language: {}", dto.getCode());
        return languageRepository.save(language);
    }

    @Transactional
    public Language updateLanguage(String code, LanguageDto dto) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + code));

        if (!code.equals(dto.getCode()) && languageRepository.existsByCode(dto.getCode())) {
            throw new EntityExistsException("Language already exists with code: " + dto.getCode());
        }

        language.setCode(dto.getCode().toLowerCase());
        language.setName(dto.getName());

        log.info("Updating language: {}", code);
        return languageRepository.save(language);
    }

    @Transactional(readOnly = true)
    public Language getLanguage(String code) {
        return languageRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + code));
    }

    @Transactional(readOnly = true)
    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }

    @Transactional
    public void deleteLanguage(String code) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + code));

        if (!language.getTranslations().isEmpty()) {
            throw new IllegalStateException("Cannot delete language that has translations");
        }

        log.info("Deleting language: {}", code);
        languageRepository.delete(language);
    }
}
