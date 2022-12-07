package com.glfx.orderprocessingservice.market;

import com.glfx.orderprocessingservice.DTO.MarketData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MarketDataRepo implements ApplicationRunner {
    private List<MarketData> exchange1DataRepository;
    private List<MarketData> exchange2DataRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}