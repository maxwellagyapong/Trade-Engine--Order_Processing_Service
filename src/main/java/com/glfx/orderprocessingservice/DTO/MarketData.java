package com.glfx.orderprocessingservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
public class MarketData {

    private String TICKER;
    private int SELL_LIMIT;
    private double LAST_TRADED_PRICE;
    private double BID_PRICE;
    private double MAX_PRICE_SHIFT;
    private double ASK_PRICE;
    private int BUY_LIMIT;


    @Override
    public String toString() {
        return "MarketData{" +
                "LAST_TRADED_PRICE=" + LAST_TRADED_PRICE +
                ", BID_PRICE=" + BID_PRICE +
                ", SELL_LIMIT=" + SELL_LIMIT +
                ", MAX_PRICE_SHIFT=" + MAX_PRICE_SHIFT +
                ", TICKER='" + TICKER + '\'' +
                ", ASK_PRICE=" + ASK_PRICE +
                ", BUY_LIMIT=" + BUY_LIMIT +
                '}';
    }
}
