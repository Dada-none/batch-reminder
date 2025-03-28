package com.dada.weather.shortTerm.chunk;

import com.dada.weather.shortTerm.job.application.dto.ShortTermWeatherItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@StepScope
public class ShortTermWeatherItemReader implements ItemReader<ShortTermWeatherItem>, StepExecutionListener {

    private final String serviceKey;
    private final String apiPath;
    private Iterator<ShortTermWeatherItem> iterator;
    private String requestDate;
    private final WebClient webClient;
    private boolean alreadyFetched = false;

    public ShortTermWeatherItemReader(@Value("${weather.base-url}") String baseUrl,
                                      @Value("${weather.service-key}") String serviceKey,
                                      @Value("${weather.short-term.api.path}") String apiPath) {

        this.webClient = WebClient.create(baseUrl);
        this.serviceKey = serviceKey;
        this.apiPath = apiPath;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {

        this.requestDate = stepExecution.getJobParameters().getString("requestDate");
        System.out.println("Batch 실행 시간 : " + requestDate);
    }

    @Override
    public ShortTermWeatherItem read() throws Exception {

        if((iterator == null || !iterator.hasNext()) && !alreadyFetched) {
            List<ShortTermWeatherItem> totalList = fetchShortTermWeatherItems(apiPath);
            iterator = totalList.iterator();
            alreadyFetched = true;
        }
        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }

    private List<ShortTermWeatherItem> fetchShortTermWeatherItems(String apiPath) throws JsonProcessingException { //예외처리 필요

        String baseDate = requestDate.substring(0, 8);
        int baseTimeInt = Integer.parseInt(requestDate.substring(8, 10));

        baseTimeInt = (baseTimeInt%3) != 2 ? (baseTimeInt/3-1)*3+2 : baseTimeInt;
        String baseTimeString = (baseTimeInt<10) ? "0" + baseTimeInt : String.valueOf(baseTimeInt);
        baseTimeString += "00";
        final String baseTime = baseTimeString;

        String nx = "55";
        String ny = "127";

        Mono<String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(apiPath)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("numOfRows", 1000) //한 페이지 당 가져오는 결과 수. 단기예보는 한번 조회마다 1000을 넘지 않음
                        .queryParam("pageNo", 1)
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.error("API 응답 오류 상태 코드: {}", clientResponse.statusCode());
                    return Mono.error(new IllegalStateException("API 응답 상태 코드 오류"));
                })
                .bodyToMono(String.class);

        return parseJsonToItems(response.block());
    }

    private List<ShortTermWeatherItem> parseJsonToItems(String json) throws JsonProcessingException {

        List<ShortTermWeatherItem> items = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode itemNodes = root.path("response").path("body").path("items").path("item");

        for(JsonNode node : itemNodes) {
            ShortTermWeatherItem item = mapper.treeToValue(node, ShortTermWeatherItem.class);
            items.add(item);
        }

        return items;
    }
}
