package com.dada.weather.shortTerm.job.application.dto;

import lombok.Data;

@Data
public class ShortTermForecast {

    private String forecast_uid;
    private String date;
    private String time;
    private String place_uid;
    private int sky;
    private int pty;
    private int humidity;
}
