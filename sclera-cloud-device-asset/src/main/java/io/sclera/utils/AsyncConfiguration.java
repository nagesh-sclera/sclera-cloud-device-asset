package io.sclera.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring configuration that enables asynchronous method execution and defines the task executors
 * used for {@code @Async} operations.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

	/**
	 * Provides a single-threaded task executor (core and max pool size of one) with a bounded queue,
	 * used for serialized asynchronous tasks.
	 */
	@Bean(name="singleTaskExecutor")
	public Executor getAsyncExecutorSingleThread() {
	        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
	        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
	        threadPoolTaskExecutor.setCorePoolSize(1);
	        threadPoolTaskExecutor.setMaxPoolSize(1);
	        threadPoolTaskExecutor.setQueueCapacity(600);
	        threadPoolTaskExecutor.afterPropertiesSet();
	        threadPoolTaskExecutor.initialize();
	        return threadPoolTaskExecutor;
	}
}
