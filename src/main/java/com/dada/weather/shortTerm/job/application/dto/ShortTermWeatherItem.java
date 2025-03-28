package com.dada.weather.shortTerm.job.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShortTermWeatherItem {

    @NotBlank
    private String baseDate;

    @NotBlank
    private String baseTime;

    @NotBlank
    private String fcstDate;

    @NotBlank
    private String fcstTime;

    @NotBlank
    private String category;

    @NotBlank
    private String fcstValue;

    @NotBlank
    private String nx;

    @NotBlank
    private String ny;
}

