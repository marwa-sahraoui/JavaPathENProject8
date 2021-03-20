package tourguide.gpsutil;

import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GpsUtilModule {

    @Bean
    public GpsUtil gpsUtil(){
        return new GpsUtil();
    }
}
