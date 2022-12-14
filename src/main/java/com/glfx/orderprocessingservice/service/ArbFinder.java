package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.DTO.OrderBook;
import com.glfx.orderprocessingservice.DTO.OrderToExchange;
import com.glfx.orderprocessingservice.utils.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


//Trying to automate trading
@Service
public class ArbFinder {

    private String exchange1 = "https://exchange.matraining.com/";

    private String exchange2 = "https://exchange2.matraining.com/";

    @Autowired
    private WebClient.Builder webclient;

    @Autowired
    private OrderToExchange orderToExchange;


    public boolean getArb() {

        final List<String> exchanges = List.of(exchange1, exchange2);

        final List<String> products = List.of("AMZN", "AAPL", "ORCL", "IBM", "TSLA", "GOOGL", "NFLX", "NFLX");

        for (String exchange : exchanges) {
            for (String product : products) {

                List<OrderBook> res = webclient.build()
                        .get()
                        .uri(exchange + "orderbook/" + product + "/")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<OrderBook>>() {
                        }).block();

                List<OrderBook> lowSelling = getLowSellingPrices(res);
                List<OrderBook> highBuying = getHighBuyingPrices(res);

                while (!(lowSelling.isEmpty() || highBuying.isEmpty())) {
                    OrderBook whatToBuy = lowSelling.stream()
                            .findFirst()
                            .get();

                    OrderBook whatToSellTo = highBuying.stream()
                            .findFirst()
                            .get();

                    if (whatToBuy.getPrice() < whatToSellTo.getPrice()) {

                        //Place
                        orderToExchange = new OrderToExchange(whatToBuy.getProduct(), whatToSellTo.getQuantity(),
                                whatToBuy.getPrice(), whatToBuy.getSide(), OrderType.LIMIT.toString());

                        String response1 = webclient.build().post()
                                .uri(exchange + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        orderToExchange = new OrderToExchange(whatToSellTo.getProduct(), whatToSellTo.getQuantity(),
                                whatToSellTo.getPrice(), whatToSellTo.getSide(), OrderType.LIMIT.toString());

                        String response2 = webclient.build().post()
                                .uri(exchange + "/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                .body(Mono.just(orderToExchange), OrderToExchange.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                    }

                    lowSelling.remove(0);
                    highBuying.remove(0);

                }
            }


        }

        return true;
    }

    private List<OrderBook> getLowSellingPrices(List<OrderBook> book) {

        //Get only selling orders that are still open
        List<OrderBook> openSellingOrders = new ArrayList<>(book.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("SELL") // Only selling orders
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity()).toList());


        // Create a comparator that will sort the objects by their prices
        Comparator<OrderBook> comparator = Collections.reverseOrder(Comparator.comparing(OrderBook::getPrice));

        // Sort the list of objects using the comparator
        openSellingOrders.sort(comparator);
        return openSellingOrders;
    }

    private List<OrderBook> getHighBuyingPrices(List<OrderBook> book) {

        // Get only buying orders that are still open
        List<OrderBook> openBuyingOrders = new ArrayList<>(book.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("BUY")
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity()).toList());

        // Create a comparator that will sort the objects by their prices
        Comparator<OrderBook> comparator = Collections.reverseOrder(Comparator.comparing(OrderBook::getPrice));

        // Sort the list of objects using the comparator
        openBuyingOrders.sort(comparator);
        return openBuyingOrders;
    }
}
