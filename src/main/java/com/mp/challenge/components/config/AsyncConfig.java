package com.mp.challenge.components.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AsyncConfig
 * <p>
 * Configuration class for asynchronous processing using Java 21 Virtual Threads.
 * This configuration enables high-performance async operations for the REST API.
 * <p>
 * Features:
 * <ul>
 *   <li>Virtual Threads support for improved concurrency</li>
 *   <li>Configurable thread pool for async operations</li>
 *   <li>Integration with Spring's @Async annotation</li>
 *   <li>Optimized for high-throughput scenarios</li>
 * </ul>
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 16/10/2025
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    /**
     * Configures the async executor for Virtual Threads.
     *
     * @return configured Executor bean
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        log.info("Configuring Virtual Threads executor for async operations");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configure for Virtual Threads (Java 21)
        executor.setThreadNamePrefix("virtual-thread-");
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Enable Virtual Threads
        executor.setVirtualThreads(true);
        
        executor.initialize();
        
        log.info("Virtual Threads executor configured successfully");
        return executor;
    }
    
    /**
     * Configures the Spring Task Executor for asynchronous operations.
     *
     * @return configured TaskExecutor bean
     */
    @Bean
    public TaskExecutor springTaskExecutor() {
        return new ConcurrentTaskExecutor(virtualThreadExecutor());
    }
}
