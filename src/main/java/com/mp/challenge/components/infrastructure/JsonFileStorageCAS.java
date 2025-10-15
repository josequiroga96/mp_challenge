package com.mp.challenge.components.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mp.challenge.components.exceptions.JsonReadException;
import com.mp.challenge.components.exceptions.JsonWriteException;
import com.mp.challenge.components.exceptions.ValidationException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import lombok.extern.slf4j.Slf4j;

/**
 * JsonFileStorageCAS
 * <p>
 * Thread-safe JSON file storage with Compare-And-Swap (CAS) operations and asynchronous persistence.
 * This class provides atomic read/write operations for JSON data with the following features:
 * <ul>
 *     <li>Lock-free reads using atomic references</li>
 *     <li>CAS-based updates for thread safety</li>
 *     <li>Asynchronous file persistence with debouncing</li>
 *     <li>Atomic file writes to prevent corruption</li>
 *     <li>Configurable debounce intervals for batch operations</li>
 * </ul>
 * <p>
 * The storage is designed for high-throughput scenarios where data consistency
 * is critical and file I/O should not block read operations.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @param <T> the type of data to be stored and retrieved
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Slf4j
public final class JsonFileStorageCAS<T> implements AutoCloseable {
    
    private final Path file;
    private final ObjectMapper objectMapper;
    private final AtomicReference<T> dataReference;
    private final ConcurrentLinkedQueue<T> pendingWrites;
    private final AtomicBoolean flushScheduled;
    private final ScheduledExecutorService ioExecutor;
    private final Class<T> dataType;
    private final long debounceMillis;

    /**
     * Constructs a new JsonFileStorageCAS instance.
     * <p>
     * Initializes the storage with the specified configuration and loads existing data
     * from the file, or uses the provided initial value if the file doesn't exist.
     *
     * @param file           the path to the JSON file for persistence
     * @param objectMapper   the Jackson ObjectMapper for JSON serialization/deserialization
     * @param dataType       the Class object representing the type T
     * @param initialValue   the initial value to use if no existing file is found
     * @param debounceMillis the debounce interval in milliseconds (0 for immediate writes)
     * @throws JsonReadException if there's an error reading existing data from the file
     */
    public JsonFileStorageCAS(
            Path file, 
            ObjectMapper objectMapper, 
            Class<T> dataType, 
            T initialValue, 
            long debounceMillis
    ) throws JsonReadException {
        this.file = file;
        this.objectMapper = createConfiguredObjectMapper(objectMapper);
        this.dataType = dataType;
        this.debounceMillis = Math.max(0, debounceMillis);
        this.pendingWrites = new ConcurrentLinkedQueue<>();
        this.flushScheduled = new AtomicBoolean(false);
        this.ioExecutor = createSingleThreadExecutor();
        this.dataReference = new AtomicReference<>(loadDataOrDefault(initialValue));
        
        log.info("JsonFileStorageCAS initialized for file: {} with debounce: {}ms", file, this.debounceMillis);
    }

    /**
     * Retrieves a snapshot of the current data without acquiring any locks.
     * <p>
     * This method provides lock-free read access to the current data state.
     * The returned value represents a consistent snapshot at the time of the call.
     *
     * @return the current data snapshot, never null
     */
    public T getCurrentSnapshot() {
        return dataReference.get();
    }

    /**
     * Performs an atomic update operation using Compare-And-Swap semantics.
     * <p>
     * This method applies the provided transformation function to the current data
     * and atomically updates the storage if no other thread has modified the data
     * in the meantime. The operation is retried automatically until successful.
     *
     * @param updateFunction the function to transform the current data
     * @return the new data value after the update
     * @throws ValidationException if updateFunction is null
     */
    public T updateAtomically(UnaryOperator<T> updateFunction) {
        if (updateFunction == null) {
            throw new ValidationException("updateFunction", "Update function cannot be null");
        }

        return performCompareAndSwapUpdate(updateFunction);
    }

    /**
     * Forces an immediate synchronous flush of all pending data to disk.
     * <p>
     * This method ensures that all pending writes are immediately persisted to disk
     * with full file system synchronization. Use this method when durability is
     * critical, such as before shutdown or after critical data updates.
     *
     * @throws JsonWriteException if the synchronous write operation fails
     */
    public void flushToDisk() throws JsonWriteException {
        Optional<T> latestData = getLatestPendingData();
        if (latestData.isPresent()) {
            writeDataAtomically(latestData.get(), true);
        }
    }

    /**
     * Gracefully shuts down the storage and ensures all pending data is persisted.
     * <p>
     * This method performs a best-effort final flush of any pending data before
     * shutting down the I/O executor. After calling this method, the storage
     * instance should not be used further.
     */
    @Override
    public void close() {
        log.info("Shutting down JsonFileStorageCAS for file: {}", file);
        try {
            performFinalFlush();
        } finally {
            shutdownExecutor();
        }
        log.info("JsonFileStorageCAS shutdown completed for file: {}", file);
    }

    /**
     * Creates a configured ObjectMapper instance with pretty printing enabled.
     */
    private ObjectMapper createConfiguredObjectMapper(ObjectMapper original) {
        return original.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Creates a single-threaded executor for I/O operations.
     */
    private ScheduledExecutorService createSingleThreadExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "json-file-storage-io");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Loads data from file or returns the default value if file doesn't exist or cannot be read.
     */
    private T loadDataOrDefault(T defaultValue) throws JsonReadException {
        if (!Files.exists(file)) {
            log.info("File does not exist, using default value for: {}", file);
            return defaultValue;
        }
        
        try {
            byte[] fileBytes = Files.readAllBytes(file);
            T loadedData = objectMapper.readValue(fileBytes, dataType);
            log.debug("Successfully loaded data from file: {}", file);
            return loadedData;
        } catch (IOException e) {
            log.error("Failed to read data from file: {}", file, e);
            throw new JsonReadException("Failed to read data from file: " + file, e);
        }
    }

    /**
     * Performs a Compare-And-Swap update operation with automatic retry.
     */
    private T performCompareAndSwapUpdate(UnaryOperator<T> updateFunction) {
        while (true) {
            T currentData = dataReference.get();
            T updatedData = updateFunction.apply(currentData);
            
            if (updatedData == currentData || Objects.equals(updatedData, currentData)) {
                log.debug("No changes detected, skipping write operation");
                return currentData;
            }
            
            if (dataReference.compareAndSet(currentData, updatedData)) {
                log.debug("Data updated successfully, scheduling write operation");
                scheduleAsyncWrite(updatedData);
                return updatedData;
            }
            
            log.debug("CAS operation failed due to concurrent modification, retrying");
        }
    }

    /**
     * Schedules an asynchronous write operation with debouncing.
     */
    private void scheduleAsyncWrite(T data) {
        pendingWrites.offer(data);
        scheduleAsyncFlush();
    }

    /**
     * Schedules an asynchronous flush operation with debouncing.
     */
    private void scheduleAsyncFlush() {
        if (debounceMillis == 0) {
            ioExecutor.execute(this::performAsyncFlush);
        } else if (flushScheduled.compareAndSet(false, true)) {
            ioExecutor.schedule(this::performAsyncFlush, debounceMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Performs asynchronous flush of pending writes.
     */
    private void performAsyncFlush() {
        try {
            Optional<T> latestData = getLatestPendingData();
            if (latestData.isPresent()) {
                try {
                    writeDataAtomically(latestData.get(), false);
                } catch (JsonWriteException e) {
                    log.warn("Async write failed, requeuing data for retry: {}", e.getMessage(), e);
                    scheduleAsyncWrite(latestData.get());
                }
            }
        } finally {
            flushScheduled.set(false);
            if (!pendingWrites.isEmpty()) {
                scheduleAsyncFlush();
            }
        }
    }

    /**
     * Retrieves and removes the latest pending data from the queue.
     *
     * @return Optional containing the latest pending data, or empty if no data is pending
     */
    private Optional<T> getLatestPendingData() {
        T latest = null;
        T current;
        while ((current = pendingWrites.poll()) != null) {
            latest = current;
        }
        return Optional.ofNullable(latest);
    }

    /**
     * Writes data to file atomically with optional file system synchronization.
     */
    private void writeDataAtomically(T data, boolean syncToDisk) throws JsonWriteException {
        log.debug("Writing data atomically to file: {} (sync: {})", file, syncToDisk);
        try {
            ensureDirectoryExists();
            Path tempFile = createTempFile();
            serializeToFile(data, tempFile);
            
            if (syncToDisk) {
                forceSyncToDisk(tempFile);
            }
            
            moveTempFileToFinalLocation(tempFile);
            
            if (syncToDisk) {
                forceSyncToDisk(file);
            }
            
            log.debug("Successfully wrote data to file: {}", file);
            
        } catch (IOException e) {
            log.error("Failed to write data to file: {}", file, e);
            scheduleAsyncWrite(data);
            throw new JsonWriteException("Failed to write data to file: " + file, e);
        }
    }

    /**
     * Ensures the parent directory exists for the target file.
     */
    private void ensureDirectoryExists() throws IOException {
        Path parentDir = file.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Creates a temporary file for atomic writing.
     */
    private Path createTempFile() {
        return file.resolveSibling(file.getFileName() + ".tmp");
    }

    /**
     * Serializes data to the specified file.
     */
    private void serializeToFile(T data, Path targetFile) throws IOException {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(data);
        Files.write(targetFile, jsonBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Forces file system synchronization for the specified file.
     */
    private void forceSyncToDisk(Path targetFile) throws IOException {
        try (FileChannel channel = FileChannel.open(targetFile, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    /**
     * Moves the temporary file to its final location atomically.
     */
    private void moveTempFileToFinalLocation(Path tempFile) throws IOException {
        try {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Performs final flush during shutdown.
     */
    private void performFinalFlush() {
        try {
            Optional<T> latestData = getLatestPendingData();
            if (latestData.isPresent()) {
                writeDataAtomically(latestData.get(), true);
            }
        } catch (JsonWriteException e) {
            log.error("Failed to perform final flush during shutdown: {}", e.getMessage(), e);
        }
    }

    /**
     * Shuts down the I/O executor gracefully.
     */
    private void shutdownExecutor() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
