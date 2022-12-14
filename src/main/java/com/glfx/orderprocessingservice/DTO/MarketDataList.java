package com.glfx.orderprocessingservice.DTO;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MarketDataList {

    private List<MarketData> marketDataList = new ArrayList<>();

    private static MarketDataList marketDataList_instance = null;

    private MarketDataList(List<MarketData> marketDataList) {
        this.marketDataList = marketDataList;
    }

    public static MarketDataList getInstance(){
        if(marketDataList_instance == null){
            marketDataList_instance = new MarketDataList();
        }
        return marketDataList_instance;
    }
}
