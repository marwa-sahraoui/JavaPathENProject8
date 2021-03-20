package tourGuide.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tourGuide.model.ProviderDTO;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TripPricerClient {

    @Value("${trip-pricer-url}")
    private String tripPricerUrl;

    public TripPricerClient() {
    }

    public TripPricerClient(String tripPricerUrl) {
        this.tripPricerUrl = tripPricerUrl;
    }

    public List<ProviderDTO> getPrice(String apiKey, UUID attractionId, int adults, int children, int nightsStay, int rewardsPoints) {

        RestTemplate restTemplate = new RestTemplate();

        String address = "http://" + tripPricerUrl + ":8083/tripPricer/Price/apiKey/" + apiKey + "/attraction/" + attractionId + "/adults/" + adults + "/children/" + children +
                "/nightsStay/" + nightsStay + "/rewardsPoints/" + rewardsPoints;

        ResponseEntity<ProviderDTO[]> response = restTemplate.getForEntity(address, ProviderDTO[].class);

        return Arrays.asList(response.getBody());
    }
}
