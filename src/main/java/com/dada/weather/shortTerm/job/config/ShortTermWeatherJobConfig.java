package com.dada.weather.shortTerm.job.config;

import com.dada.weather.shortTerm.chunk.ShortTermWeatherItemProcessor;
import com.dada.weather.shortTerm.chunk.ShortTermWeatherItemReader;
import com.dada.weather.shortTerm.chunk.ShortTermWeatherItemWriter;
import com.dada.weather.shortTerm.job.application.dto.ShortTermWeatherItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableBatchProcessing
public class ShortTermWeatherJobConfig {

    @Bean
    public Job shortTermWeatherChunkJob(JobRepository jobRepository, Step shortTermWeatherChunkStep) {
        return new JobBuilder("weatherChunkJob", jobRepository)
                .start(shortTermWeatherChunkStep)
                .build();
    }

    @Bean
    public Step shortTermWeatherChunkStep(JobRepository jobRepository,
                                          PlatformTransactionManager platformTransactionManager,
                                          ShortTermWeatherItemReader reader,
                                          ShortTermWeatherItemProcessor processor,
                                          ShortTermWeatherItemWriter writer) {
        return new StepBuilder("weatherChunkStep", jobRepository)
                .<ShortTermWeatherItem, ShortTermWeatherItem>chunk(2000, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
