package com.glfx.orderprocessingservice.market.marketRepository;

import com.glfx.orderprocessingservice.market.marketDTO.MarketData;
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

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://exchange.matraining.com/md/")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MarketDataService mdService = retrofit.create(MarketDataService.class);

            Call<List<MarketData>> md1 = mdService.getMarketData("https://exchange.matraining.com/md");
            Call<List<MarketData>> md2 = mdService.getMarketData("https://exchange2.matraining.com/md");



            exchange1DataRepository = md1.execute().body();
            exchange2DataRepository = md2.execute().body();


        }

}
