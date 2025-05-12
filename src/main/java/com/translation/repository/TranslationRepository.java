package com.translation.repository;

import com.translation.model.Translation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    
    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags WHERE t.language.code = :languageCode")
    List<Translation> findAllByLanguageCode(@Param("languageCode") String languageCode);

    @Query("SELECT t FROM Translation t LEFT JOIN FETCH t.tags WHERE t.translationKey = :key AND t.language.code = :languageCode")
    Optional<Translation> findByKeyAndLanguageCode(@Param("key") String key, @Param("languageCode") String languageCode);

    @Query("SELECT DISTINCT t FROM Translation t " +
           "LEFT JOIN FETCH t.tags tags " +
           "WHERE t.content LIKE %:searchTerm% " +
           "OR t.translationKey LIKE %:searchTerm% " +
           "OR tags.name IN :tags")
    Page<Translation> searchTranslations(
        @Param("searchTerm") String searchTerm,
        @Param("tags") Set<String> tags,
        Pageable pageable
    );

    @Query("SELECT t FROM Translation t " +
           "LEFT JOIN FETCH t.tags " +
           "WHERE t.language.code = :languageCode " +
           "AND t.translationKey IN :keys")
    List<Translation> findByKeysAndLanguageCode(
        @Param("keys") Set<String> keys,
        @Param("languageCode") String languageCode
    );
}
