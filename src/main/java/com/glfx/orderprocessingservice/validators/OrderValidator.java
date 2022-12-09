package com.glfx.orderprocessingservice.validators;

import com.glfx.orderprocessingservice.DTO.MarketData;
import com.glfx.orderprocessingservice.DTO.PortfolioStock;
import com.glfx.orderprocessingservice.DAOs.PortfolioRepository;
import com.glfx.orderprocessingservice.DAOs.MarketDataRepo;
import com.glfx.orderprocessingservice.exceptions.InvalidOrderException;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.DTO.Portfolio;
import com.glfx.orderprocessingservice.utils.OrderType;
import com.glfx.orderprocessingservice.utils.Side;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Getter
public class OrderValidator {
    private String exchanges;

    @Autowired
    MarketDataRepo marketDataRepo;

    @Autowired
    private WebClient.Builder webclient;

    @Autowired
    PortfolioRepository portfolioRepository;

    public boolean validate(Order order) throws InvalidOrderException {
//        if(order.getQuantity() == null){
//
//        }
        boolean result = false;

        //Get market data on ordered product from exchange 1
        MarketData orderMD1 = marketDataRepo.productMarketDataFromExchange1(order);

        //Get market data on ordered product from exchange 2
        MarketData orderMD2 = marketDataRepo.productMarketDataFromExchange2(order);

        //select the right exchange based on order validation
        if (isValidOrder(order,orderMD1) && isValidOrder(order,orderMD2)){
            //valid on both exchanges
            this.exchanges = "both";
            result = true;
        }else if (!isValidOrder(order,orderMD1) && isValidOrder(order,orderMD2)){
            //valid on exchange 2
            this.exchanges = "exchange2";
            result = true;
        }else if (isValidOrder(order,orderMD1) && !isValidOrder(order,orderMD2)){
            //valid on exchange 1
            this.exchanges = "exchange1";
            result = true;
        }

        //if order passes all order validation rules return true else return false
        return result;
    }

    private boolean isQuantityValid(Order order, MarketData md) throws InvalidOrderException{
        boolean isValid;
        if(order.getSide() == null){
            throw new InvalidOrderException("Order must have a side, either BUY or SELL");
        }
        else{
            if(order.getSide().equals(Side.BUY.toString())){
                isValid = !(order.getQuantity() > md.getBUY_LIMIT());
                if(isValid){
                    return isValid;
                }
                else throw new InvalidOrderException("The quantity you want to buy is more than the current buy limit! Please reduce the quantity and try again.");
            }
            else if(order.getSide().equals(Side.SELL.toString())){
                isValid = !(order.getQuantity() > md.getSELL_LIMIT());
                if(isValid){
                    return isValid;
                }
                else throw new InvalidOrderException("The quantity you want to sell is more than the current sell limit! Please reduce the quantity and try again.");
            }
        }

        return false;
    }

    private boolean isPriceReasonable(Order order, MarketData md) throws InvalidOrderException {
        boolean isReasonable;

        if(order.getSide().equals(Side.BUY.toString())){
                isReasonable = !(Math.abs(md.getLAST_TRADED_PRICE() - order.getPrice()) > md.getMAX_PRICE_SHIFT());
        }else{
            isReasonable = !(Math.abs(order.getPrice() - md.getLAST_TRADED_PRICE() ) > md.getMAX_PRICE_SHIFT());
        }

        if(isReasonable)
            return isReasonable;
        else
            if(order.getPrice() - md.getLAST_TRADED_PRICE() > 0){
                throw new InvalidOrderException("Order price is too high!");
            }
            else throw new InvalidOrderException("Order price is too low!");

    }

    private boolean hasEnoughResources(Order order) throws InvalidOrderException {
        boolean enoughResources;

        if(order.getType().equalsIgnoreCase(OrderType.LIMIT.toString())){
            if(order.getSide().equals(Side.BUY.toString())){

                Portfolio buyerPortfolio = portfolioRepository.getClientPortfolioFromClientService(order);

                enoughResources = buyerPortfolio.getAmount() >= (order.getQuantity() * order.getPrice());
                if(enoughResources){
                    return enoughResources;
                }
                else {
                    throw new InvalidOrderException("You do not have enough balance to buy " + order.getQuantity() + " " + order.getProduct() + " stocks!");
                }

            }
            else{
                PortfolioStock sellerStock = portfolioRepository.getStockFromPortfolioByTickerWhenSelling(order);

                enoughResources = sellerStock.getQuantity() >= order.getQuantity();
                if(enoughResources){
                    return enoughResources;
                }
                else {
                    throw new InvalidOrderException("You do not have enough " + order.getProduct() + " stocks to sell!");
                }
            }
        }

        else {
            if(order.getSide().equals(Side.BUY.toString())){

                Portfolio buyerPortfolio = portfolioRepository.getClientPortfolioFromClientService(order);

                enoughResources = buyerPortfolio.getAmount() >= (order.getQuantity() * marketDataRepo.productMarketDataFromExchange1(order).getASK_PRICE());
                if(enoughResources){
                    return enoughResources;
                }
                else {
                    throw new InvalidOrderException("You do not have enough balance to buy " + order.getQuantity() + " " + order.getProduct() + " stocks!");
                }

            }
            else{
                PortfolioStock sellerStock = portfolioRepository.getStockFromPortfolioByTickerWhenSelling(order);

                enoughResources = sellerStock.getQuantity() >= order.getQuantity();
                if(enoughResources){
                    return enoughResources;
                }
                else {
                    throw new InvalidOrderException("You do not have enough " + order.getProduct() + " stocks to sell!");
                }
            }
        }


    }

    private boolean isValidOrder(Order order, MarketData md) throws InvalidOrderException {
        if(order.getType() == null){
            throw new InvalidOrderException("Order must have a type. Either LIMIT or MARKET");
        }
        else {
            if (order.getType().equals(OrderType.LIMIT.toString())) {
                if(order.getPrice() == null){
                    throw new InvalidOrderException("You must set a price for LIMIT orders!");
                }
                else return isQuantityValid(order, md) && isPriceReasonable(order, md) && hasEnoughResources(order);

            } else if (order.getType().equals(OrderType.MARKET.toString())) {

                return isQuantityValid(order, md) && hasEnoughResources(order);
            }
        }

        return false;
    }

}
