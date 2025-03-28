package com.dada.weather.shortTerm.chunk;

import com.dada.weather.shortTerm.job.application.dto.ShortTermWeatherItem;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ShortTermWeatherItemProcessor implements ItemProcessor<ShortTermWeatherItem, ShortTermWeatherItem> {

    @Override
    public ShortTermWeatherItem process(ShortTermWeatherItem item) throws Exception {

        int fcstTimeToInt = Integer.parseInt(item.getFcstTime().substring(0, 2));
        String category = item.getCategory();

        if(((fcstTimeToInt >= 0 && fcstTimeToInt <= 2) || (fcstTimeToInt >= 21 && fcstTimeToInt <= 23)) &&
                ((category.equals("SKY") || (category.equals("PTY") || (category.equals("REH")))))){
            return item;
        }
        else return null;
    }
}
