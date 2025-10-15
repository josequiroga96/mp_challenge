package com.mp.challenge.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mp.challenge.exceptions.JsonReadException;
import com.mp.challenge.exceptions.JsonWriteException;
import com.mp.challenge.models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemRepositoryCAS.
 * <p>
 * Tests focus on success cases and important failure scenarios for the public methods
 * of ItemRepositoryCAS, ensuring thread-safe operations and proper data persistence.
 */
class ItemRepositoryCASTest {

    private ObjectMapper objectMapper;
    private Path testFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        testFile = tempDir.resolve("items.json");
    }

    @Test
    void constructor_ShouldInitializeSuccessfully_WhenValidParameters() throws JsonReadException {
        // When & Then
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            assertNotNull(repository);
        }
    }

    @Test
    void constructor_ShouldThrowJsonReadException_WhenFileIsCorrupted() throws Exception {
        // Given - Create corrupted JSON file
        java.nio.file.Files.write(testFile, "invalid json content".getBytes());

        // When & Then
        assertThrows(JsonReadException.class, () -> 
                new ItemRepositoryCAS(testFile, objectMapper)
        );
    }

    @Test
    void save_ShouldCreateNewItem_WhenItemHasNoId() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            Item newItem = createTestItem(null, "Test Item", "https://example.com/image.jpg");

            // When
            Item savedItem = repository.save(newItem);

            // Then
            assertNotNull(savedItem);
            assertNotNull(savedItem.getId());
            assertEquals("Test Item", savedItem.getName());
            assertNotNull(savedItem.getCreatedAt());
            assertNotNull(savedItem.getUpdatedAt());
        }
    }

    @Test
    void save_ShouldUpdateExistingItem_WhenItemHasId() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            UUID itemId = UUID.randomUUID();
            Item originalItem = createTestItem(itemId, "Original Name", "https://example.com/image.jpg");
            Item savedItem = repository.save(originalItem);

            Item updatedItem = createTestItem(itemId, "Updated Name", "https://example.com/updated.jpg");

            // When
            Item result = repository.save(updatedItem);

            // Then
            assertNotNull(result);
            assertEquals(itemId, result.getId());
            assertEquals("Updated Name", result.getName());
            assertEquals("https://example.com/updated.jpg", result.getImageUrl());
            assertEquals(savedItem.getCreatedAt(), result.getCreatedAt()); // createdAt should remain the same
            assertTrue(result.getUpdatedAt().isAfter(savedItem.getUpdatedAt())); // updatedAt should be newer
        }
    }

    @Test
    void save_ShouldThrowIllegalArgumentException_WhenItemIsNull() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        }
    }

    @Test
    void findById_ShouldReturnItem_WhenItemExists() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            Item testItem = createTestItem(null, "Find Me", "https://example.com/find.jpg");
            Item savedItem = repository.save(testItem);

            // When
            Optional<Item> result = repository.findById(savedItem.getId());

            // Then
            assertTrue(result.isPresent());
            assertEquals(savedItem.getId(), result.get().getId());
            assertEquals("Find Me", result.get().getName());
        }
    }

    @Test
    void findById_ShouldReturnEmpty_WhenItemDoesNotExist() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            UUID nonExistentId = UUID.randomUUID();

            // When
            Optional<Item> result = repository.findById(nonExistentId);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Test
    void findById_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
        }
    }

    @Test
    void findAll_ShouldReturnAllItems_WhenItemsExist() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            Item item1 = createTestItem(null, "Item 1", "https://example.com/item1.jpg");
            Item item2 = createTestItem(null, "Item 2", "https://example.com/item2.jpg");
            
            repository.save(item1);
            repository.save(item2);

            // When
            List<Item> result = repository.findAll();

            // Then
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(i -> "Item 1".equals(i.getName())));
            assertTrue(result.stream().anyMatch(i -> "Item 2".equals(i.getName())));
        }
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoItemsExist() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            // When
            List<Item> result = repository.findAll();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void deleteById_ShouldReturnDeletedItem_WhenItemExists() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            Item testItem = createTestItem(null, "To Delete", "https://example.com/delete.jpg");
            Item savedItem = repository.save(testItem);

            // When
            Optional<Item> result = repository.deleteById(savedItem.getId());

            // Then
            assertTrue(result.isPresent());
            assertEquals(savedItem.getId(), result.get().getId());
            assertEquals("To Delete", result.get().getName());
            
            // Verify item is no longer findable
            assertFalse(repository.findById(savedItem.getId()).isPresent());
        }
    }

    @Test
    void deleteById_ShouldReturnEmpty_WhenItemDoesNotExist() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            UUID nonExistentId = UUID.randomUUID();

            // When
            Optional<Item> result = repository.deleteById(nonExistentId);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Test
    void deleteById_ShouldThrowIllegalArgumentException_WhenIdIsNull() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> repository.deleteById(null));
        }
    }

    @Test
    void flushToDisk_ShouldNotThrowException_WhenCalled() throws JsonReadException, JsonWriteException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            Item testItem = createTestItem(null, "Flush Test", "https://example.com/flush.jpg");
            repository.save(testItem);

            // When & Then - should not throw
            assertDoesNotThrow(() -> repository.flushToDisk());
        }
    }

    @Test
    void close_ShouldNotThrowException_WhenCalled() throws JsonReadException {
        // Given
        ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper);

        // When & Then - should not throw
        assertDoesNotThrow(() -> repository.close());
    }

    @Test
    void concurrentOperations_ShouldBeThreadSafe_WhenMultipleThreadsAccess() throws JsonReadException, InterruptedException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile, objectMapper)) {
            int threadCount = 5;
            int itemsPerThread = 10;
            Thread[] threads = new Thread[threadCount];

            // When - Create multiple threads that save items concurrently
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < itemsPerThread; j++) {
                        try {
                            Item item = createTestItem(null, 
                                    "Thread-" + threadId + "-Item-" + j, 
                                    "https://example.com/thread" + threadId + "item" + j + ".jpg");
                            repository.save(item);
                        } catch (Exception e) {
                            fail("Thread " + threadId + " failed: " + e.getMessage());
                        }
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then - Verify all items were saved correctly
            List<Item> allItems = repository.findAll();
            assertEquals(threadCount * itemsPerThread, allItems.size());
            
            // Verify no duplicate IDs
            long uniqueIds = allItems.stream().map(Item::getId).distinct().count();
            assertEquals(allItems.size(), uniqueIds);
        }
    }

    /**
     * Helper method to create a test item with the specified parameters.
     *
     * @param id the ID for the item (null for new items)
     * @param name the name of the item
     * @param imageUrl the image URL of the item
     * @return a new Item instance
     */
    private Item createTestItem(UUID id, String name, String imageUrl) {
        return new Item(
                id,
                name,
                imageUrl,
                "Test description",
                new BigDecimal("99.99"),
                new BigDecimal("4.5"),
                List.of("Spec 1", "Spec 2"),
                null, // createdAt will be set by repository
                null  // updatedAt will be set by repository
        );
    }
}
