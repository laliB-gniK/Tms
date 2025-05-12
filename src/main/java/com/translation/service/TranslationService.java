package com.translation.service;

import com.translation.dto.TranslationDto;
import com.translation.model.Language;
import com.translation.model.Tag;
import com.translation.model.Translation;
import com.translation.repository.LanguageRepository;
import com.translation.repository.TagRepository;
import com.translation.repository.TranslationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final LanguageRepository languageRepository;
    private final TagRepository tagRepository;

    @Transactional
    @CacheEvict(value = {"translations", "translationsByLanguage"}, allEntries = true)
    public Translation createTranslation(TranslationDto dto) {
        Language language = languageRepository.findByCode(dto.getLanguageCode())
                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + dto.getLanguageCode()));

        Set<Tag> tags = getOrCreateTags(dto.getTags());

        Translation translation = new Translation();
        translation.setTranslationKey(dto.getKey());
        translation.setContent(dto.getContent());
        translation.setLanguage(language);
        translation.setTags(tags);

        return translationRepository.save(translation);
    }

    @Transactional
    @CacheEvict(value = {"translations", "translationsByLanguage"}, allEntries = true)
    public Translation updateTranslation(String key, String languageCode, TranslationDto dto) {
        Translation translation = translationRepository.findByKeyAndLanguageCode(key, languageCode)
                .orElseThrow(() -> new EntityNotFoundException("Translation not found"));

        translation.setContent(dto.getContent());
        
        if (dto.getTags() != null) {
            Set<Tag> tags = getOrCreateTags(dto.getTags());
            translation.setTags(tags);
        }

        return translationRepository.save(translation);
    }

    @Transactional(readOnly = true)
    public Page<Translation> searchTranslations(String searchTerm, Set<String> tags, int page, int size) {
        return translationRepository.searchTranslations(
            searchTerm != null ? searchTerm : "",
            tags != null ? tags : new HashSet<>(),
            PageRequest.of(page, size)
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "translations", key = "#key + '_' + #languageCode")
    public Translation getTranslation(String key, String languageCode) {
        return translationRepository.findByKeyAndLanguageCode(key, languageCode)
                .orElseThrow(() -> new EntityNotFoundException("Translation not found"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "translationsByLanguage", key = "#languageCode")
    public Map<String, Object> getTranslationsForLanguage(String languageCode) {
        List<Translation> translations = translationRepository.findAllByLanguageCode(languageCode);
        
        Map<String, Object> result = new HashMap<>();
        for (Translation translation : translations) {
            String[] keyParts = translation.getTranslationKey().split("\\.");
            buildNestedStructure(result, keyParts, translation.getContent());
        }
        
        return result;
    }

    private void buildNestedStructure(Map<String, Object> current, String[] keyParts, String value) {
        for (int i = 0; i < keyParts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(keyParts[i], k -> new HashMap<>());
        }
        current.put(keyParts[keyParts.length - 1], value);
    }

    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> existingTags = tagRepository.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    return tagRepository.save(tag);
                })
                .collect(Collectors.toSet());

        existingTags.addAll(newTags);
        return existingTags;
    }

    @Transactional
    @CacheEvict(value = {"translations", "translationsByLanguage"}, allEntries = true)
    public void deleteTranslation(String key, String languageCode) {
        Translation translation = translationRepository.findByKeyAndLanguageCode(key, languageCode)
                .orElseThrow(() -> new EntityNotFoundException("Translation not found"));
        translationRepository.delete(translation);
    }
}
