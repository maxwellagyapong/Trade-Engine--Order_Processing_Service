package com.glfx.orderprocessingservice.service;

import com.glfx.orderprocessingservice.DAOs.OrderBookRepository;
import com.glfx.orderprocessingservice.DTO.OrderBook;
import com.glfx.orderprocessingservice.DTO.OrderBookWithExchange;
import com.glfx.orderprocessingservice.DTO.OrderToExchange;
import com.glfx.orderprocessingservice.model.Leg;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.repository.LegRepository;
import com.glfx.orderprocessingservice.repository.OrderRepository;
import com.glfx.orderprocessingservice.utils.Exchange;
import com.glfx.orderprocessingservice.utils.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SplitOrderService {

    private final String exchange1Book = "https://exchange2.matraining.com/orderbook/";

    private final String exchange2Book = "https://exchange2.matraining.com/orderbook/";

    private final String exchange1 = "https://exchange.matraining.com";

    private final String exchange2 = "https://exchange2.matraining.com";

    @Autowired
    private OrderBookRepository orderBookRepository;

    @Autowired
    private OrderToExchange orderToExchange;

    @Autowired
    private WebClient.Builder webclient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Leg leg;

    @Autowired
    private LegRepository legRepository;



    public void splitOrderForLimitOrder(Order order) {

        //Get orderBook on the ordered product from exchange 1
        List<OrderBook> orderBook1 = orderBookRepository.getProductOrderBook(order, exchange1Book);

        //Add the exchange to them
        List<OrderBookWithExchange> updatedOrderBook1 = addRightExchangeTag(orderBook1, Exchange.exchange1.toString());

        //Get orderBook on the ordered product from exchange 2
        List<OrderBook> orderBook2 = orderBookRepository.getProductOrderBook(order, exchange2Book);

        //Add the exchange to them
        //Helps to track which exchange each open order is from, since we are merging books from two exchanges
        List<OrderBookWithExchange> updatedOrderBook2 = addRightExchangeTag(orderBook2, Exchange.exchange2.toString());

        //Analyze and execute
        executeOrders(updatedOrderBook1, updatedOrderBook2, order);
    }


    private List<OrderBookWithExchange> getLowSellingPrices(List<OrderBookWithExchange> book1, List<OrderBookWithExchange> book2) {

        //Merge the two books
        List<OrderBookWithExchange> fullOrderBook = mergeOrderBooks(book1, book2);

        //Get only selling orders that are still open
        List<OrderBookWithExchange> openSellingOrders = new ArrayList<>(fullOrderBook.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("SELL") // Only selling orders
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity()).toList());


        // Create a comparator that will sort the objects by their prices
        Comparator<OrderBookWithExchange> comparator = Collections.reverseOrder(Comparator.comparing(OrderBookWithExchange::getPrice));

        // Sort the list of objects using the comparator
        openSellingOrders.sort(comparator);
        return openSellingOrders;
    }


    private List<OrderBookWithExchange> getHighBuyingPrices(List<OrderBookWithExchange> book1, List<OrderBookWithExchange> book2) {

        // Merge the two order books
        List<OrderBookWithExchange> fullOrderBook = mergeOrderBooks(book1, book2);

        // Get only buying orders that are still open
        List<OrderBookWithExchange> openBuyingOrders = new ArrayList<>(fullOrderBook.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("BUY")
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity()).toList());

        // Create a comparator that will sort the objects by their prices
        Comparator<OrderBookWithExchange> comparator = Collections.reverseOrder(Comparator.comparing(OrderBookWithExchange::getPrice));

        // Sort the list of objects using the comparator
        openBuyingOrders.sort(comparator);
        return openBuyingOrders;
    }


    //Merge the two Order Books
    private List<OrderBookWithExchange> mergeOrderBooks(List<OrderBookWithExchange> orderBook1, List<OrderBookWithExchange> orderBook2) {

        return Stream.concat(orderBook1.stream(), orderBook2.stream()).collect(Collectors.toList());
    }

    //Helps to track which exchange each open order is from, since we are merging books from two exchanges
    private List<OrderBookWithExchange> addRightExchangeTag(List<OrderBook> book, String exchange){

        List<OrderBookWithExchange> updateDatedWithExchanges = new ArrayList<>();

        for(OrderBook b: book){
            updateDatedWithExchanges.add(new OrderBookWithExchange(b.getProduct(), b.getPrice(), b.getQuantity(),
                    b.getSide(), b.getOrderID(), b.getOrderType(), b.getCumulatitiveQuantity(),
                    b.getCumulatitivePrice(), b.getExecutions(), exchange));
        }

        return updateDatedWithExchanges;
    }


    private void executeOrders(List<OrderBookWithExchange> d1, List<OrderBookWithExchange> d2, Order order) {
        order.setStatus(Status.NOT_EXECUTED.toString());
        orderRepository.save(order);//First save order to DB

        if (order.getSide().equalsIgnoreCase("BUY")) {

            //Get the sorted list of low selling prices on both exchange
            List<OrderBookWithExchange> ListOfWhatToBuy = getLowSellingPrices(d1, d2);


            int amountBought = 0;

            while(amountBought < order.getQuantity()){

                //Must ensure list is not empty first
                if(ListOfWhatToBuy.isEmpty()){

                    //Just place order to any of the exchanges, with quantity left to buy
                    orderToExchange = new OrderToExchange(order.getProduct(), order.getQuantity(), order.getPrice(),
                            order.getSide(), order.getType());

                    //Take to exchange 1
                    String response = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                    break;
                }

                else {
                    OrderBookWithExchange whatToBuy = ListOfWhatToBuy.stream()
                            .findFirst()
                            .get();
                    if(order.getQuantity() > whatToBuy.getQuantity()) {

                        orderToExchange = new OrderToExchange(order.getProduct(),
                                whatToBuy.getQuantity(), order.getPrice(),
                                order.getSide(), order.getType());

                        //Take order to the right exchange
                        if(whatToBuy.getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){

                            //Take to exchange 1
                            String response = webclient.build().post()
                                    .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }
                        else{

                            //Take to exchange 2
                            String response = webclient.build().post()
                                    .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }


                        amountBought += whatToBuy.getQuantity();
                        //remove already bought data from the order book and move to the next one
                        ListOfWhatToBuy.remove(0);
                    }
                    else {
                        //Just buy it
                        orderToExchange = new OrderToExchange(order.getProduct(), order.getQuantity(), order.getPrice(),
                                order.getSide(), order.getType());

                        //Take order to the right exchange
                        if(whatToBuy.getExchange().equalsIgnoreCase(Exchange.exchange1.toString())){

                            //Take to exchange 1
                            String response = webclient.build().post()
                                    .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }
                        else{

                            //Take to exchange 2
                            String response = webclient.build().post()
                                    .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }
                        break;
                    }
                }
            }

        }

        else {
            //Get high buying prices
            List<OrderBookWithExchange> ListOfWhatToSellTo = getHighBuyingPrices(d1, d2);


            int amountSold = 0;

            while(amountSold < order.getQuantity()){

                //Must ensure list is not empty first
                if(ListOfWhatToSellTo.isEmpty()){

                    //Just place order to any of the exchanges, with quantity left to buy
                    orderToExchange = new OrderToExchange(order.getProduct(),
                            order.getQuantity() - amountSold, order.getPrice(),
                            order.getSide(), order.getType());

                    //Take to exchange 1
                    String response = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }
                else {

                    OrderBookWithExchange whatToSellTo = ListOfWhatToSellTo.stream()
                            .findFirst()
                            .get();
                    if(order.getQuantity() > whatToSellTo.getQuantity()) {

                        orderToExchange = new OrderToExchange(order.getProduct(),
                                whatToSellTo.getQuantity(), order.getPrice(),
                                order.getSide(), order.getType());

                        //Take order to the right exchange
                        if(whatToSellTo.getExchange().equalsIgnoreCase("exchange1")){

                            //Take to exchange 1
                            String response = webclient.build().post()
                                    .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }
                        else{

                            //Take to exchange 2
                            String response = webclient.build().post()
                                    .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }

                        amountSold += whatToSellTo.getQuantity();
                        //remove already bought data from the order book and move to the next one
                        ListOfWhatToSellTo.remove(0);
                    }
                    else {

                        //Just sell it
                        orderToExchange = new OrderToExchange(order.getProduct(), order.getQuantity(), order.getPrice(),
                                order.getSide(), order.getType());

                        //Take order to the right exchange
                        if(whatToSellTo.getExchange().equalsIgnoreCase("exchange1")){

                            //Take to exchange 1
                            String response = webclient.build().post()
                                    .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }
                        else{

                            //Take to exchange 2
                            String response = webclient.build().post()
                                    .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                                    .body(Mono.just(orderToExchange), OrderToExchange.class)
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();

                            leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                                    orderToExchange.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                            legRepository.save(leg);
                        }
                        break;
                    }
                }
            }
        }


    }



    //FOR MARKET ORDERS
    public void splitOrderForMarketOrder(Order order){

        //Get orderBook on a specific product from exchange 1
        List<OrderBook> orderBook1 = orderBookRepository.getProductOrderBook(order, exchange1Book);

        //Get orderBook on a specific product from exchange 2
        List<OrderBook> orderBook2 = orderBookRepository.getProductOrderBook(order, exchange2Book);

        //compare marketData and return list
        compareDataAndExecuteOrders(orderBook1 ,orderBook2, order);

    }


    private void compareDataAndExecuteOrders(List<OrderBook> d1, List<OrderBook> d2, Order order){
        order.setStatus(Status.NOT_EXECUTED.toString());
        orderRepository.save(order); //First save order in DB

        if (order.getSide().equalsIgnoreCase("BUY")){

            //Get the average selling price from both exchanges
            double avgSellPrice1 = getAverageSellingPrice(d1);
            double avgSellPrice2 = getAverageSellingPrice(d2);

            if (avgSellPrice1 <= avgSellPrice2){
                //Get the available quantity from exchange1
                int available = getAvailableQuantity(d1, order.getSide());

                if (available >= order.getQuantity()){
                    //No splitting
                    //Take to exchange 1

                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity()
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            order.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                }

                else{//If we can't get all the quantity on exchange 1
                    //Split
                    //Take this to exchange 1
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response1 = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save First leg
                    leg = new Leg(response1.substring(1,response1.length()-1), Exchange.exchange1.toString(), order.getProduct(), available,
                            order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);


                    //Take this to exchange 2
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity() - available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response2 = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save Second leg
                    leg = new Leg(response2.substring(1,response2.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            order.getQuantity() - available, order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }

            }

            else{//if avgSellPrice2<avgSellPrice1
                //Get the available quantity from exchange2
                int available = getAvailableQuantity(d2, order.getSide());

                if (available >= order.getQuantity()){

                    //No splitting
                    //Take to exchange 2
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity()
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            order.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }
                else{//If we can't get all the quantity on exchange 2
                    //Split
                    //Take this to exchange 2
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response1 = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save first leg
                    leg = new Leg(response1.substring(1,response1.length()-1), Exchange.exchange2.toString(), order.getProduct(), available,
                            order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                    //Take this to exchange 1
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity() - available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response2 = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save second leg
                    leg = new Leg(response2.substring(1,response2.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            order.getQuantity() - available, order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }
            }


        }
        else{//If it is a SELL order
            //Get the average buying price from both exchanges
            double avg_bid1 = getAverageBuyingPrice(d1);
            double avg_bid2 = getAverageBuyingPrice(d2);

            if (avg_bid1 >= avg_bid2){
                //Get available quantity from exchange1
                int available = getAvailableQuantity(d1, order.getSide());

                if (available >= order.getQuantity()){
                    //No splitting
                    //Take to exchange 1
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity()
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save leg
                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            order.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }

                else{//If we can't get all the quantity on exchange 1
                    //Split
                    //Take this to exchange 1
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response1 = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save first leg
                    leg = new Leg(response1.substring(1,response1.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            available, order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                    //Take this to exchange 2
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity() - available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response2 = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save second leg
                    leg = new Leg(response2.substring(1,response2.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            order.getQuantity() - available, order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }

            }else{//If avg_bid2 < avg_bid1
                //Get available quantity from exchange2
                int available = getAvailableQuantity(d2, order.getSide());

                if (available >= order.getQuantity()){

                    //No splitting
                    //Take this to exchange2
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity()
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save leg
                    leg = new Leg(response.substring(1,response.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            order.getQuantity(), order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                }
                else{//If we can't get all the quantity on exchange 2
                    //Split
                    //Take this to exchange 2
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , available
                            , order.getPrice()
                            , order.getSide()
                            , order.getType());

                    String response1 = webclient.build().post()
                            .uri(exchange2+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save leg 1
                    leg = new Leg(response1.substring(1,response1.length()-1), Exchange.exchange2.toString(), order.getProduct(),
                            available, order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);

                    //Take this to exchange1
                    orderToExchange = new OrderToExchange(order.getProduct()
                            , order.getQuantity() - available
                            , order.getPrice()
                            , order.getSide()
                            ,order.getType());

                    String response2 = webclient.build().post()
                            .uri(exchange1+"/15a204cd-9f59-45e9-8908-fbfe7f20480d/order")
                            .body(Mono.just(orderToExchange), OrderToExchange.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    //Save leg 2
                    leg = new Leg(response2.substring(1,response2.length()-1), Exchange.exchange1.toString(), order.getProduct(),
                            order.getQuantity() - available, order.getSide(), order.getStatus(), order.getId());
                    legRepository.save(leg);
                }
            }

        }

    }


    private double getAverageSellingPrice(List<OrderBook> orders){

        double sumSellingPrices = orders.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("SELL") // Only selling orders
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity()) // Only orders that are not completed
                .map(OrderBook::getPrice)
                .reduce(Double::sum).get();

        double numberOfSellingOrders = orders.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("SELL")
                        && openOrder.getExecutions().isEmpty())
                .count();

        return sumSellingPrices/numberOfSellingOrders;

    }

    private double getAverageBuyingPrice(List<OrderBook> orders){
        double sumBuyingPrices = orders.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("buy")
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity())
                .map(OrderBook::getPrice)
                .reduce(Double::sum).get();

        double numberOfBuyingOrders = orders.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase("buy")
                        && openOrder.getExecutions().isEmpty())
                .count();

        return sumBuyingPrices/numberOfBuyingOrders;

    }

    private int getAvailableQuantity(List<OrderBook> orders, String side){

        return orders.stream()
                .filter(openOrder -> openOrder.getSide().equalsIgnoreCase(side.equalsIgnoreCase("BUY")?
                        "SELL" : "BUY")
                        && openOrder.getCumulatitiveQuantity() < openOrder.getQuantity())
                .map(openOrder -> openOrder.getQuantity() - openOrder.getCumulatitiveQuantity())
                .reduce(Integer::sum).get();
    }
}
