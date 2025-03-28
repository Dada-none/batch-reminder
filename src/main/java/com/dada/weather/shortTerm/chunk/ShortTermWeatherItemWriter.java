package com.dada.weather.shortTerm.chunk;

import com.dada.weather.shortTerm.job.application.dto.ShortTermForecast;
import com.dada.weather.shortTerm.job.application.dto.ShortTermWeatherItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ShortTermWeatherItemWriter implements ItemWriter<ShortTermWeatherItem> {

    private final JdbcTemplate jdbcTemplate;

    public ShortTermWeatherItemWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends ShortTermWeatherItem> items) throws Exception {

        if(items.isEmpty()) return;

        Map<String, List<ShortTermWeatherItem>> groupedItemsByDateAndTime = items.getItems().stream()
                .collect(Collectors.groupingBy(
                        item -> item.getFcstDate() + item.getFcstTime()
                ));

        groupedItemsByDateAndTime.forEach((dateTime, groupedList) -> {
            ShortTermForecast forecast = aggregateItemsByDateAndTime(dateTime, groupedList);
            saveForecast(forecast);
        });
    }

    private ShortTermForecast aggregateItemsByDateAndTime(String dateTime, List<ShortTermWeatherItem> items) {

        log.info("{}시 그룹의 항목 수: {}", dateTime, items.size());

        ShortTermForecast forecast = new ShortTermForecast();
        ShortTermWeatherItem first = items.getFirst();
        forecast.setForecast_uid(dateTime + "_" + first.getNx() + "_" + first.getNy());
        forecast.setPlace_uid(first.getNx() + "_" + first.getNy());
        forecast.setDate(dateTime.substring(0, 8));
        forecast.setTime(dateTime.substring(8, 10));
        log.info("setForecast_uid => dateTime : {}, item.getNx() : {}, item.getNy() : {}", dateTime, first.getNx(), first.getNy());

        //SKY
        items.stream()
                .filter(item -> "SKY".equals(item.getCategory()))
                .findFirst()
                .ifPresent(item -> {
                    log.info("setSky() : {}", item.getFcstValue());
                    forecast.setSky(Integer.parseInt(item.getFcstValue()));

                });

        //PTY
        items.stream()
                .filter(item -> "PTY".equals(item.getCategory()))
                .findFirst()
                .ifPresent(item -> {
                    log.info("setPty() : {}", item.getFcstValue());
                    forecast.setPty(Integer.parseInt(item.getFcstValue()));
                });

        //REH
        items.stream()
                .filter(item -> "REH".equals(item.getCategory()))
                .findFirst()
                .ifPresent(item -> {
                    log.info("setHumidity() : {}", item.getFcstValue());
                    forecast.setHumidity(Integer.parseInt(item.getFcstValue()));
                });

        return forecast;
    }

    private void saveForecast(ShortTermForecast forecast) {

        jdbcTemplate.update(
            "INSERT INTO forecast (forecast_uid, date, time, place_uid, sky, pty, humidity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                        "sky = VALUES(sky), " +
                        "pty = VALUES(pty), " +
                        "humidity = VALUES(humidity);",
            forecast.getForecast_uid(),
            forecast.getDate(),
            forecast.getTime(),
            forecast.getPlace_uid(),
            forecast.getSky(),
            forecast.getPty(),
            forecast.getHumidity()
        );
    }
}
