package io.sclera.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

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
