package com.mp.challenge.components.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mp.challenge.components.exceptions.JsonReadException;
import com.mp.challenge.components.exceptions.JsonWriteException;
import com.mp.challenge.components.exceptions.ValidationException;
import com.mp.challenge.components.infrastructure.JsonFileStorageCAS;
import com.mp.challenge.helpers.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileStorageCASTest {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private Path testFile;
    private TestData initialData;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        testFile = tempDir.resolve("test-data.json");
        initialData = new TestData("initial", 42, LocalDateTime.now());
    }

    @Test
    void constructor_ShouldCreateInstanceWithDefaultValue_WhenFileDoesNotExist() throws JsonReadException {
        // When
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        )) {
            // Then
            assertNotNull(storage);
            TestData snapshot = storage.getCurrentSnapshot();
            assertEquals(initialData, snapshot);
            assertFalse(Files.exists(testFile));
        }
    }

    @Test
    void constructor_ShouldLoadExistingData_WhenFileExists() throws IOException, JsonReadException {
        // Given
        TestData existingData = new TestData("existing", 100, LocalDateTime.now());
        objectMapper.writeValue(testFile.toFile(), existingData);

        // When
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        )) {
            // Then
            TestData snapshot = storage.getCurrentSnapshot();
            assertEquals(existingData.getName(), snapshot.getName());
            assertEquals(existingData.getValue(), snapshot.getValue());
        }
    }

    @Test
    void constructor_ShouldThrowJsonReadException_WhenFileIsCorrupted() throws IOException {
        // Given
        Files.write(testFile, "invalid json content".getBytes());

        // When & Then
        assertThrows(JsonReadException.class, () ->
                new JsonFileStorageCAS<>(testFile, objectMapper, TestData.class, initialData, 1000)
        );
    }

    @Test
    void getCurrentSnapshot_ShouldReturnCurrentData_WhenCalled() throws JsonReadException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        )) {
            // When
            TestData snapshot = storage.getCurrentSnapshot();

            // Then
            assertEquals(initialData, snapshot);
        }
    }

    @Test
    void updateAtomically_ShouldUpdateDataSuccessfully_WhenValidFunctionProvided() throws JsonReadException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 100
        )) {
            // When
            TestData updated = storage.updateAtomically(data ->
                    new TestData("updated", data.getValue() + 1, LocalDateTime.now())
            );

            // Then
            assertEquals("updated", updated.getName());
            assertEquals(43, updated.getValue());
            assertEquals(updated, storage.getCurrentSnapshot());
        }
    }

    @Test
    void updateAtomically_ShouldNotWriteToDisk_WhenFunctionReturnsSameData() throws JsonReadException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 100
        )) {
            AtomicInteger callCount = new AtomicInteger(0);

            // When - function that returns the same data
            TestData result = storage.updateAtomically(data -> {
                callCount.incrementAndGet();
                return data; // Return same reference
            });

            // Then
            assertEquals(initialData, result);
            assertEquals(1, callCount.get());
            assertFalse(Files.exists(testFile));
        }
    }

    @Test
    void updateAtomically_ShouldThrowValidationException_WhenFunctionIsNull() throws JsonReadException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        )) {
            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    storage.updateAtomically(null)
            );
            
            assertEquals("VALIDATION_FAILED", exception.getErrorCode());
            assertTrue(exception.getDetails().containsKey("updateFunction"));
            assertEquals("Update function cannot be null", exception.getDetails().get("updateFunction"));
        }
    }

    @Test
    void flushToDisk_ShouldWriteDataToFile_WhenPendingDataExists() throws JsonReadException, JsonWriteException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        )) {
            // Make an update to create pending data
            storage.updateAtomically(data ->
                    new TestData("flushed", 999, LocalDateTime.now())
            );

            // When
            storage.flushToDisk();

            // Then
            assertTrue(Files.exists(testFile));
            try {
                TestData writtenData = objectMapper.readValue(testFile.toFile(), TestData.class);
                assertEquals("flushed", writtenData.getName());
                assertEquals(999, writtenData.getValue());
            } catch (Exception e) {
                fail("Failed to read written data: " + e.getMessage());
            }
        }
    }

    @Test
    void flushToDisk_ShouldNotThrowException_WhenNoPendingData() throws JsonReadException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        )) {
            // When & Then - should not throw
            assertDoesNotThrow(storage::flushToDisk);
            assertFalse(Files.exists(testFile));
        }
    }

    @Test
    void close_ShouldFlushPendingDataAndShutdownGracefully() throws JsonReadException, IOException {
        // Given
        JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 1000
        );

        // Make an update
        storage.updateAtomically(data ->
                new TestData("before_close", 888, LocalDateTime.now())
        );

        // When
        storage.close();

        // Then - file should exist with the data
        assertTrue(Files.exists(testFile));
        TestData writtenData = objectMapper.readValue(testFile.toFile(), TestData.class);
        assertEquals("before_close", writtenData.getName());
        assertEquals(888, writtenData.getValue());
    }

    @Test
    void concurrentUpdates_ShouldHandleMultipleThreadsCorrectly() throws JsonReadException, InterruptedException {
        // Given
        try (JsonFileStorageCAS<TestData> storage = new JsonFileStorageCAS<>(
                testFile, objectMapper, TestData.class, initialData, 100
        )) {
            int threadCount = 10;
            int updatesPerThread = 5;
            Thread[] threads = new Thread[threadCount];

            // When - multiple threads updating concurrently
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < updatesPerThread; j++) {
                        storage.updateAtomically(data ->
                                new TestData("thread-" + threadId, data.getValue() + 1, LocalDateTime.now())
                        );
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            TestData finalData = storage.getCurrentSnapshot();
            assertNotNull(finalData);
            assertTrue(finalData.getName().startsWith("thread-"));

            // Value should be initial + total updates (threadCount * updatesPerThread)
            assertEquals(initialData.getValue() + (threadCount * updatesPerThread), finalData.getValue());
        }
    }
}
