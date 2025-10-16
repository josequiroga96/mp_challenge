package com.mp.challenge.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mp.challenge.components.exceptions.JsonReadException;
import com.mp.challenge.components.exceptions.JsonWriteException;
import com.mp.challenge.components.exceptions.ValidationException;
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
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            assertNotNull(repository);
        }
    }

    @Test
    void constructor_ShouldThrowJsonReadException_WhenFileIsCorrupted() throws Exception {
        // Given - Create corrupted JSON file
        java.nio.file.Files.write(testFile, "invalid json content".getBytes());

        // When & Then
        assertThrows(JsonReadException.class, () -> 
                new ItemRepositoryCAS(testFile.toFile(), objectMapper)
        );
    }

    @Test
    void save_ShouldCreateNewItem_WhenItemHasNoId() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
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
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
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
    void save_ShouldThrowValidationException_WhenItemIsNull() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> repository.save(null));
            assertEquals("VALIDATION_FAILED", exception.getErrorCode());
            assertTrue(exception.getDetails().containsKey("item"));
            assertEquals("Item cannot be null", exception.getDetails().get("item"));
        }
    }

    @Test
    void findById_ShouldReturnItem_WhenItemExists() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
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
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            UUID nonExistentId = UUID.randomUUID();

            // When
            Optional<Item> result = repository.findById(nonExistentId);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Test
    void findById_ShouldThrowValidationException_WhenIdIsNull() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> repository.findById(null));
            assertEquals("VALIDATION_FAILED", exception.getErrorCode());
            assertTrue(exception.getDetails().containsKey("id"));
            assertEquals("ID cannot be null", exception.getDetails().get("id"));
        }
    }

    @Test
    void findAll_ShouldReturnAllItems_WhenItemsExist() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
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
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // When
            List<Item> result = repository.findAll();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void deleteById_ShouldReturnDeletedItem_WhenItemExists() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
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
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            UUID nonExistentId = UUID.randomUUID();

            // When
            Optional<Item> result = repository.deleteById(nonExistentId);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Test
    void deleteById_ShouldThrowValidationException_WhenIdIsNull() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> repository.deleteById(null));
            assertEquals("VALIDATION_FAILED", exception.getErrorCode());
            assertTrue(exception.getDetails().containsKey("id"));
            assertEquals("ID cannot be null", exception.getDetails().get("id"));
        }
    }

    @Test
    void flushToDisk_ShouldNotThrowException_WhenCalled() throws JsonReadException, JsonWriteException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            Item testItem = createTestItem(null, "Flush Test", "https://example.com/flush.jpg");
            repository.save(testItem);

            // When & Then - should not throw
            assertDoesNotThrow(() -> repository.flushToDisk());
        }
    }

    @Test
    void close_ShouldNotThrowException_WhenCalled() throws JsonReadException {
        // Given
        ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper);

        // When & Then - should not throw
        assertDoesNotThrow(() -> repository.close());
    }

    @Test
    void concurrentOperations_ShouldBeThreadSafe_WhenMultipleThreadsAccess() throws JsonReadException, InterruptedException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
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

    @Test
    void saveAndFindById_ShouldReturnSameObject_WhenItemIsSavedAndRetrieved() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            Item originalItem = createTestItem(null, "Same Object Test", "https://example.com/same.jpg");

            // When
            Item savedItem = repository.save(originalItem);
            Optional<Item> retrievedItem = repository.findById(savedItem.getId());

            // Then
            assertTrue(retrievedItem.isPresent());
            // Verify they are the same object (same ID, name, and all properties)
            assertEquals(savedItem.getId(), retrievedItem.get().getId());
            assertEquals(savedItem.getName(), retrievedItem.get().getName());
            assertEquals(savedItem.getImageUrl(), retrievedItem.get().getImageUrl());
            assertEquals(savedItem.getDescription(), retrievedItem.get().getDescription());
            assertEquals(savedItem.getPrice(), retrievedItem.get().getPrice());
            assertEquals(savedItem.getRating(), retrievedItem.get().getRating());
            assertEquals(savedItem.getSpecifications(), retrievedItem.get().getSpecifications());
            assertEquals(savedItem.getCreatedAt(), retrievedItem.get().getCreatedAt());
            assertEquals(savedItem.getUpdatedAt(), retrievedItem.get().getUpdatedAt());
        }
    }

    @Test
    void save_ShouldIncreaseCollectionSizeByOne_WhenNewItemIsAdded() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // Initial state
            List<Item> initialItems = repository.findAll();
            int initialSize = initialItems.size();

            // When
            Item newItem = createTestItem(null, "Size Test", "https://example.com/size.jpg");
            repository.save(newItem);

            // Then
            List<Item> finalItems = repository.findAll();
            assertEquals(initialSize + 1, finalItems.size());
            assertTrue(finalItems.stream().anyMatch(item -> "Size Test".equals(item.getName())));
        }
    }

    @Test
    void update_ShouldNotAffectOtherItems_WhenOneItemIsUpdated() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // Create multiple items
            Item item1 = createTestItem(null, "Item 1", "https://example.com/item1.jpg");
            Item item2 = createTestItem(null, "Item 2", "https://example.com/item2.jpg");
            Item item3 = createTestItem(null, "Item 3", "https://example.com/item3.jpg");
            
            Item savedItem1 = repository.save(item1);
            Item savedItem2 = repository.save(item2);
            Item savedItem3 = repository.save(item3);

            // Capture original state of other items
            String originalName2 = savedItem2.getName();
            String originalUrl2 = savedItem2.getImageUrl();
            String originalName3 = savedItem3.getName();
            String originalUrl3 = savedItem3.getImageUrl();

            // When - Update only item1
            Item updatedItem1 = createTestItem(savedItem1.getId(), "Updated Item 1", "https://example.com/updated1.jpg");
            repository.save(updatedItem1);

            // Then - Verify other items are unchanged
            Optional<Item> retrievedItem2 = repository.findById(savedItem2.getId());
            Optional<Item> retrievedItem3 = repository.findById(savedItem3.getId());
            
            assertTrue(retrievedItem2.isPresent());
            assertTrue(retrievedItem3.isPresent());
            
            assertEquals(originalName2, retrievedItem2.get().getName());
            assertEquals(originalUrl2, retrievedItem2.get().getImageUrl());
            assertEquals(originalName3, retrievedItem3.get().getName());
            assertEquals(originalUrl3, retrievedItem3.get().getImageUrl());
            
            // Verify item1 was actually updated
            Optional<Item> retrievedItem1 = repository.findById(savedItem1.getId());
            assertTrue(retrievedItem1.isPresent());
            assertEquals("Updated Item 1", retrievedItem1.get().getName());
            assertEquals("https://example.com/updated1.jpg", retrievedItem1.get().getImageUrl());
        }
    }

    @Test
    void data_ShouldPersist_WhenRepositoryIsRecreated() throws JsonReadException {
        // Given
        Item savedItem;
        UUID savedItemId;
        
        // Save item in first repository instance
        try (ItemRepositoryCAS repository1 = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            Item testItem = createTestItem(null, "Persistence Test", "https://example.com/persist.jpg");
            savedItem = repository1.save(testItem);
            savedItemId = savedItem.getId();
        }

        // When - Create new repository instance and retrieve data
        try (ItemRepositoryCAS repository2 = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            Optional<Item> retrievedItem = repository2.findById(savedItemId);

            // Then
            assertTrue(retrievedItem.isPresent());
            assertEquals(savedItem.getId(), retrievedItem.get().getId());
            assertEquals(savedItem.getName(), retrievedItem.get().getName());
            assertEquals(savedItem.getImageUrl(), retrievedItem.get().getImageUrl());
            assertEquals(savedItem.getDescription(), retrievedItem.get().getDescription());
            assertEquals(savedItem.getPrice(), retrievedItem.get().getPrice());
            assertEquals(savedItem.getRating(), retrievedItem.get().getRating());
        }
    }

    @Test
    void comprehensiveCrudWorkflow_ShouldWorkCorrectly_WhenPerformingFullLifecycle() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // CREATE - Add multiple items
            Item item1 = createTestItem(null, "Workflow Item 1", "https://example.com/workflow1.jpg");
            Item item2 = createTestItem(null, "Workflow Item 2", "https://example.com/workflow2.jpg");
            Item item3 = createTestItem(null, "Workflow Item 3", "https://example.com/workflow3.jpg");
            
            Item savedItem1 = repository.save(item1);
            Item savedItem2 = repository.save(item2);
            Item savedItem3 = repository.save(item3);

            // READ - Verify all items exist
            List<Item> allItems = repository.findAll();
            assertEquals(3, allItems.size());
            
            Optional<Item> foundItem1 = repository.findById(savedItem1.getId());
            Optional<Item> foundItem2 = repository.findById(savedItem2.getId());
            Optional<Item> foundItem3 = repository.findById(savedItem3.getId());
            
            assertTrue(foundItem1.isPresent());
            assertTrue(foundItem2.isPresent());
            assertTrue(foundItem3.isPresent());

            // UPDATE - Modify item2
            Item updatedItem2 = createTestItem(savedItem2.getId(), "Updated Workflow Item 2", "https://example.com/updated2.jpg");
            Item resultItem2 = repository.save(updatedItem2);
            
            assertEquals(savedItem2.getId(), resultItem2.getId());
            assertEquals("Updated Workflow Item 2", resultItem2.getName());
            assertEquals("https://example.com/updated2.jpg", resultItem2.getImageUrl());
            assertEquals(savedItem2.getCreatedAt(), resultItem2.getCreatedAt()); // createdAt should remain
            assertTrue(resultItem2.getUpdatedAt().isAfter(savedItem2.getUpdatedAt())); // updatedAt should be newer

            // DELETE - Remove item3
            Optional<Item> deletedItem = repository.deleteById(savedItem3.getId());
            assertTrue(deletedItem.isPresent());
            assertEquals(savedItem3.getId(), deletedItem.get().getId());

            // Verify final state
            List<Item> finalItems = repository.findAll();
            assertEquals(2, finalItems.size());
            
            assertTrue(repository.findById(savedItem1.getId()).isPresent());
            assertTrue(repository.findById(savedItem2.getId()).isPresent());
            assertFalse(repository.findById(savedItem3.getId()).isPresent());
        }
    }

    @Test
    void save_ShouldHandleDuplicateIds_WhenItemWithExistingIdIsAdded() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            UUID fixedId = UUID.randomUUID();
            Item originalItem = createTestItem(fixedId, "Original", "https://example.com/original.jpg");
            repository.save(originalItem);

            // When - Try to add another item with the same ID
            Item duplicateIdItem = createTestItem(fixedId, "Duplicate ID", "https://example.com/duplicate.jpg");
            repository.save(duplicateIdItem);

            // Then - The second item should overwrite the first (update behavior)
            Optional<Item> retrievedItem = repository.findById(fixedId);
            assertTrue(retrievedItem.isPresent());
            assertEquals("Duplicate ID", retrievedItem.get().getName());
            assertEquals("https://example.com/duplicate.jpg", retrievedItem.get().getImageUrl());
            
            // Verify only one item with this ID exists
            List<Item> allItems = repository.findAll();
            long itemsWithThisId = allItems.stream()
                    .filter(item -> fixedId.equals(item.getId()))
                    .count();
            assertEquals(1, itemsWithThisId);
        }
    }

    @Test
    void deleteById_ShouldDecreaseCollectionSizeByOne_WhenExistingItemIsDeleted() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            // Add some items first
            Item item1 = createTestItem(null, "Delete Size Test 1", "https://example.com/delete1.jpg");
            Item item2 = createTestItem(null, "Delete Size Test 2", "https://example.com/delete2.jpg");
            Item item3 = createTestItem(null, "Delete Size Test 3", "https://example.com/delete3.jpg");
            
            Item savedItem1 = repository.save(item1);
            Item savedItem2 = repository.save(item2);
            Item savedItem3 = repository.save(item3);

            // Verify initial size
            List<Item> initialItems = repository.findAll();
            assertEquals(3, initialItems.size());

            // When
            Optional<Item> deletedItem = repository.deleteById(savedItem2.getId());

            // Then
            assertTrue(deletedItem.isPresent());
            List<Item> finalItems = repository.findAll();
            assertEquals(2, finalItems.size());
            
            // Verify the correct item was deleted
            assertFalse(repository.findById(savedItem2.getId()).isPresent());
            assertTrue(repository.findById(savedItem1.getId()).isPresent());
            assertTrue(repository.findById(savedItem3.getId()).isPresent());
        }
    }

    @Test
    void save_ShouldPreserveTimestamps_WhenUpdatingExistingItem() throws JsonReadException {
        // Given
        try (ItemRepositoryCAS repository = new ItemRepositoryCAS(testFile.toFile(), objectMapper)) {
            Item originalItem = createTestItem(null, "Timestamp Test", "https://example.com/timestamp.jpg");
            Item savedItem = repository.save(originalItem);
            
            // Small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When - Update the item
            Item updatedItem = createTestItem(savedItem.getId(), "Updated Timestamp Test", "https://example.com/updated-timestamp.jpg");
            Item result = repository.save(updatedItem);

            // Then
            assertEquals(savedItem.getId(), result.getId());
            assertEquals(savedItem.getCreatedAt(), result.getCreatedAt()); // createdAt should be preserved
            assertTrue(result.getUpdatedAt().isAfter(savedItem.getUpdatedAt())); // updatedAt should be newer
            assertEquals("Updated Timestamp Test", result.getName());
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
