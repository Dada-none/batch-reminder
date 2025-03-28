package com.dada.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job shortTermWeatherChunkJob;

    @Scheduled(cron = "0 0 2/3 * * *")
    public void runShortTermWeatherJob() throws Exception {

        String requestDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        JobParameters params = new JobParametersBuilder()
                .addString("requestDate", requestDate)
                .toJobParameters();

        jobLauncher.run(shortTermWeatherChunkJob, params);
    }
}
