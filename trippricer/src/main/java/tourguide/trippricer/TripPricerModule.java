package tourguide.trippricer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tripPricer.TripPricer;

@Configuration
public class TripPricerModule {

@Bean
    public TripPricer tripPricer(){
    return new TripPricer();
   }
}
