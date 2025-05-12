package com.translation.performance;

import com.translation.dto.LanguageDto;
import com.translation.dto.TranslationDto;
import com.translation.service.LanguageService;
import com.translation.service.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TranslationPerformanceTest {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private LanguageService languageService;

    private static final int TOTAL_TRANSLATIONS = 100_000;
    private static final String TEST_LANGUAGE = "en";
    private static final int BATCH_SIZE = 1000;
    private static final long EXPECTED_EXPORT_TIME_MS = 500;
    private static final long EXPECTED_SEARCH_TIME_MS = 200;

    @BeforeEach
    void setUp() {
        // Create test language
        LanguageDto languageDto = new LanguageDto();
        languageDto.setCode(TEST_LANGUAGE);
        languageDto.setName("English");
        try {
            languageService.createLanguage(languageDto);
        } catch (Exception e) {
            // Language might already exist
        }
    }

    @Test
    void testBulkTranslationCreationPerformance() {
        List<Long> batchTimes = new ArrayList<>();
        int totalCreated = 0;

        for (int batch = 0; batch < TOTAL_TRANSLATIONS; batch += BATCH_SIZE) {
            long startTime = System.currentTimeMillis();
            
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = batch; i < batch + BATCH_SIZE && i < TOTAL_TRANSLATIONS; i++) {
                final int index = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    createTestTranslation(index);
                });
                futures.add(future);
            }

            // Wait for all translations in this batch to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            long batchTime = System.currentTimeMillis() - startTime;
            batchTimes.add(batchTime);
            totalCreated += BATCH_SIZE;
            
            System.out.printf("Created %d translations in %d ms (%.2f ms per translation)%n",
                    BATCH_SIZE, batchTime, (double) batchTime / BATCH_SIZE);
        }

        double avgBatchTime = batchTimes.stream().mapToLong(Long::valueOf).average().orElse(0.0);
        System.out.printf("Average batch creation time: %.2f ms%n", avgBatchTime);
    }

    @Test
    void testExportPerformance() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> translations = translationService.getTranslationsForLanguage(TEST_LANGUAGE);
        long executionTime = System.currentTimeMillis() - startTime;

        System.out.printf("Export execution time: %d ms%n", executionTime);
        assertTrue(executionTime < EXPECTED_EXPORT_TIME_MS,
                String.format("Export took %d ms, expected less than %d ms", executionTime, EXPECTED_EXPORT_TIME_MS));
    }

    @Test
    void testSearchPerformance() {
        // Test search performance with different criteria
        List<String> searchTerms = Arrays.asList("common", "button", "error", "success");
        List<Set<String>> tagSets = Arrays.asList(
            Set.of("web"),
            Set.of("mobile"),
            Set.of("web", "mobile"),
            Set.of("desktop")
        );

        for (String searchTerm : searchTerms) {
            for (Set<String> tags : tagSets) {
                long startTime = System.currentTimeMillis();
                translationService.searchTranslations(searchTerm, tags, 0, 20);
                long executionTime = System.currentTimeMillis() - startTime;

                System.out.printf("Search execution time (term: %s, tags: %s): %d ms%n",
                        searchTerm, tags, executionTime);
                assertTrue(executionTime < EXPECTED_SEARCH_TIME_MS,
                        String.format("Search took %d ms, expected less than %d ms", executionTime, EXPECTED_SEARCH_TIME_MS));
            }
        }
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int numThreads = 50;
        Random random = new Random();
        
        // Create a list of operations to perform
        List<Runnable> operations = Arrays.asList(
            // Read operations
            () -> translationService.getTranslationsForLanguage(TEST_LANGUAGE),
            () -> translationService.searchTranslations("test", Set.of("web"), 0, 20),
            // Write operations
            () -> createTestTranslation(random.nextInt(10000))
        );

        List<Thread> threads = IntStream.range(0, numThreads)
            .mapToObj(i -> new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        operations.get(random.nextInt(operations.size())).run();
                    } catch (Exception e) {
                        fail("Concurrent operation failed: " + e.getMessage());
                    }
                }
            }))
            .collect(Collectors.toList());

        long startTime = System.currentTimeMillis();
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        long executionTime = System.currentTimeMillis() - startTime;

        System.out.printf("Concurrent operations completed in %d ms%n", executionTime);
        assertTrue(executionTime < TimeUnit.SECONDS.toMillis(10),
                "Concurrent operations took too long to complete");
    }

    private void createTestTranslation(int index) {
        TranslationDto dto = new TranslationDto();
        dto.setKey(String.format("test.key.%d", index));
        dto.setContent(String.format("Test content %d", index));
        dto.setLanguageCode(TEST_LANGUAGE);
        dto.setTags(Set.of("web", "mobile", "test"));
        
        translationService.createTranslation(dto);
    }
}
