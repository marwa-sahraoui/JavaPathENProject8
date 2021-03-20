package tourGuide.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class RewardCentralClient {

    @Value("${reward-central-url}")
    private String rewardCentralUrl;

    public RewardCentralClient() {
    }

    public RewardCentralClient(String rewardCentralUrl) {
        this.rewardCentralUrl = rewardCentralUrl;
    }

    public Integer getAttractionRewardPoints(UUID attractionId, UUID userId) {

        RestTemplate restTemplate = new RestTemplate();

        String address = "http://" + rewardCentralUrl + ":8082/rewardCentral/attractionRewardPoints/attraction/" + attractionId + "/user/" + userId;

        ResponseEntity<Integer> response = restTemplate.getForEntity(address, Integer.class);

        return response.getBody();
    }

}
