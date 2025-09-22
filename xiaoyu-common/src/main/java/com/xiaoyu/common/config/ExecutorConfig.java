package com.xiaoyu.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExecutorConfig {

    @Bean("cacheRebuildExecutor")
    public ThreadPoolTaskExecutor cacheRebuildExecutor(
            // 以下值都可以放到 application.yml，用 @Value 注入
            @Value("${executor.userinfo.core:8}") int core,
            @Value("${executor.userinfo.max:16}") int max,
            @Value("${executor.userinfo.queue:1024}") int queue) {

        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(core);
        exec.setMaxPoolSize(max);
        exec.setQueueCapacity(queue);
        exec.setKeepAliveSeconds(60*30);
        exec.setThreadNamePrefix("cache-rebuild-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.setWaitForTasksToCompleteOnShutdown(true);  // 优雅关闭
        exec.initialize();
        return exec;
    }
}
