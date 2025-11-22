package edu.ucsal.fiadopay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class TaskExecutorConfig {

    @Bean("asyncTaskExecutor")
    public ExecutorService asyncTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}