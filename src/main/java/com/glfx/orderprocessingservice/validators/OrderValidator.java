package com.glfx.orderprocessingservice.validators;

import com.glfx.orderprocessingservice.market.marketDTO.MarketData;
import com.glfx.orderprocessingservice.market.marketRepository.MarketDataRepo;
import com.glfx.orderprocessingservice.model.Order;
import com.glfx.orderprocessingservice.utils.Side;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class OrderValidator {
        private String exchanges;
        //Get market data from the repo
        @Autowired
        MarketDataRepo marketDataRepo;


        public boolean validate(Order order){

            boolean result = false;

            //Find market data on specific product
            MarketData orderMD1 = marketDataRepo.getExchange1DataRepository()
                    .stream()
                    .filter(md -> md.getTICKER().equalsIgnoreCase(order.getProduct().toString()))
                    .findFirst()
                    .get();


            MarketData orderMD2 = marketDataRepo.getExchange2DataRepository()
                    .stream()
                    .filter(md -> md.getTICKER().equalsIgnoreCase(order.getProduct().toString()))
                    .findFirst()
                    .get();

            //select the right exchange based on order validation
            if (isValidOrder(order,orderMD1) && isValidOrder(order,orderMD2)){
                //valid on both exchanges
                this.exchanges = "both";
                result = true;
            }else if (!isValidOrder(order,orderMD1) && isValidOrder(order,orderMD2)){
                //valid on exchange 2
                this.exchanges = "ex2";
                result = true;
            }else if (isValidOrder(order,orderMD1) && !isValidOrder(order,orderMD2)){
                //valid on exchange 1
                this.exchanges = "ex1";
                result = true;
            }

            //if order passes all order validation rules return true else return false
            return result;
        }

        private boolean isQuantityValid(Order order, MarketData md){
            boolean isValid;

            if(order.getSide().equals(Side.Buy)){
                isValid = !(order.getQuantity() > md.getBUY_LIMIT());
            }
            else{
                isValid = !(order.getQuantity() > md.getSELL_LIMIT());
            }

            return isValid;
        }

        private boolean isPriceReasonable(Order order, MarketData md){
            boolean isReasonable;

            if(order.getSide().equals(Side.Buy)){
                isReasonable = !((md.getLAST_TRADED_PRICE() - order.getQuotedPrice()) > md.getMAX_PRICE_SHIFT());
            }else{
                isReasonable = !((order.getQuotedPrice() - md.getLAST_TRADED_PRICE() ) > md.getMAX_PRICE_SHIFT());
            }

            return isReasonable;
        }

        private boolean hasEnoughResources(Order order){
            boolean enoughResources;

            if(order.getSide().equals(Side.Buy)){
                enoughResources = order.getPortfolio().getFunds() > (order.getQuantity() * order.getQuotedPrice());
            }else{
                enoughResources = order.getPortfolio().getQuantityOwned() > order.getQuantity();
            }

            return enoughResources;
        }

        private boolean isValidOrder(Order order, MarketData md){
            return isPriceReasonable(order, md) && isQuantityValid(order, md) && hasEnoughResources(order);
        }

}
